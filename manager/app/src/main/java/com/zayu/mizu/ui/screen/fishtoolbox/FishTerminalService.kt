package com.zayu.mizu.ui.screen.fishtoolbox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zayu.mizu.R
import com.zayu.mizu.ui.MainActivity
import java.io.File

/**
 * 前台服务 — ProcessBuilder 生成 shell，输出桥接到 Compose UI。
 *
 * 不依赖 ATE 的 initializeEmulator（在某些设备 PTY 创建失败导致 NPE 崩溃）。
 * 直接用 ProcessBuilder 启动 /system/bin/sh，stdout 流式输出给 UI。
 */
class FishTerminalService : Service() {

    private val binder = LocalBinder()

    @Volatile var isRunning: Boolean = false
    @Volatile var initError: String? = null

    private var shellProcess: Process? = null
    private var stdoutThread: Thread? = null
    private var onOutput: ((String) -> Unit)? = null
    private var onExit: ((Int) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): FishTerminalService = this@FishTerminalService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val scriptPath = intent.getStringExtra(EXTRA_SCRIPT_PATH)
                val cwd = intent.getStringExtra(EXTRA_CWD) ?: filesDir.absolutePath + "/" + SCRIPT_SUBDIR
                startShell(scriptPath, cwd, null)
            }
            ACTION_STOP -> stopShell()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        onOutput = null; onExit = null
        stopShell()
    }

    // ── Shell 管理 ──

    fun startShell(scriptPath: String?, cwd: String, onOutputCb: ((String) -> Unit)?) {
        if (isRunning) return
        initError = null
        onOutput = onOutputCb

        try {
            // 智能选择 shell: 内置 bash > Termux bash > /system/bin/sh
            val localBash = File(filesDir, "bash")
            val termuxBash = "/data/data/com.termux/files/usr/bin/bash"
            val shell = when {
                localBash.canExecute() -> localBash.absolutePath
                File(termuxBash).canExecute() -> termuxBash
                else -> "/system/bin/sh"
            }
            val hasScript = !scriptPath.isNullOrEmpty() && File(scriptPath).exists()

            val pb = if (hasScript) {
                ProcessBuilder(shell, scriptPath!!)
            } else {
                ProcessBuilder(shell, "-i")
            }
            pb.directory(File(cwd))
            pb.redirectErrorStream(true)
            val env = pb.environment()
            env["TERM"] = "xterm-256color"
            env["LANG"] = "en_US.UTF-8"
            if (shell != termuxBash) {
                // mksh: 设置兼容环境
                env["ENV"] = "/system/bin/sh"
            }

            val proc = pb.start()
            shellProcess = proc

            // 读取 stdout → 回调给 UI
            stdoutThread = Thread {
                try {
                    val reader = proc.inputStream.bufferedReader()
                    val buf = CharArray(4096)
                    var n: Int
                    while (reader.read(buf).also { n = it } != -1) {
                        val text = String(buf, 0, n)
                        onOutput?.invoke(text)
                    }
                } catch (_: Exception) {}
            }.apply { start() }

            // 监控进程退出
            Thread {
                try {
                    val code = proc.waitFor()
                    shellProcess = null
                    isRunning = false
                    onExit?.invoke(code)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } catch (_: Exception) {}
            }.start()

            isRunning = true
            startForeground(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            isRunning = false
            initError = "${e.javaClass.simpleName}: ${e.message}"
        }
    }

    fun stopShell() {
        shellProcess?.destroy()
        shellProcess = null
        stdoutThread?.interrupt()
        stdoutThread = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /** 向 shell stdin 写入 */
    fun writeStdin(text: String) {
        try {
            shellProcess?.outputStream?.apply {
                write(text.toByteArray())
                flush()
            }
        } catch (_: Exception) {}
    }

    fun setOnOutput(cb: (String) -> Unit) { onOutput = cb }
    fun setOnExit(cb: (Int) -> Unit) { onExit = cb }

    // ── 通知 ──

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getService(this, 1,
            Intent(this, FishTerminalService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🐟 杂鱼工具箱").setContentText("终端运行中…")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setOngoing(true)
            .setContentIntent(pi).addAction(android.R.drawable.ic_media_pause, "停止", stop).build()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm?.getNotificationChannel(CHANNEL_ID) == null) {
            nm?.createNotificationChannel(NotificationChannel(CHANNEL_ID, "杂鱼工具箱",
                NotificationManager.IMPORTANCE_LOW).apply { description = "杂鱼工具箱终端运行通知"; setShowBadge(false) })
        }
    }

    companion object {
        const val ACTION_START = "com.zayu.mizu.action.FISH_START"
        const val ACTION_STOP = "com.zayu.mizu.action.FISH_STOP"
        const val EXTRA_SCRIPT_PATH = "script_path"
        const val EXTRA_CWD = "cwd"
        const val CHANNEL_ID = "fish_toolbox_terminal"
        const val NOTIFICATION_ID = 7733
        const val SCRIPT_SUBDIR = "fish_toolbox"
    }
}
