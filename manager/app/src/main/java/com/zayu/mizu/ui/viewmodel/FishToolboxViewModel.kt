package com.zayu.mizu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zayu.mizu.ui.screen.fishtoolbox.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class FishToolboxViewModel(private val baseDir: File) : ViewModel() {

    private val _uiState = MutableStateFlow(FishToolboxUiState())
    val uiState: StateFlow<FishToolboxUiState> = _uiState.asStateFlow()

    private val scriptDir = File(baseDir, SCRIPT_SUBDIR).also { it.mkdirs() }
    val scriptFile = File(scriptDir, SCRIPT_NAME)

    init {
        viewModelScope.launch {
            if (scriptFile.exists()) {
                _uiState.update {
                    it.copy(
                        scriptPath = scriptFile.absolutePath,
                        localVersion = readLocalVersion()
                    )
                }
            }
        }
    }

    // ── 新流程 API ──

    /** Step 1: 检查云端版本 → 弹出更新弹窗 */
    fun checkVersion() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScreenPhase.Checking, downloadState = DownloadState.Checking) }
            try {
                val json = withContext(Dispatchers.IO) { downloadText(VERSION_URL) }
                val obj = JSONObject(json)
                val remoteVersion = obj.optString("version", "")
                val remoteSha256 = obj.optString("sha256", "")
                val changelog = obj.optString("changelog", "")
                val downloadUrl = obj.optString("url", "")
                val localVersion = readLocalVersion()

                if (downloadUrl.isEmpty()) {
                    _uiState.update {
                        it.copy(phase = ScreenPhase.Idle, downloadState = DownloadState.Failed("version.json 缺少 url"))
                    }
                    return@launch
                }

                // 本地已有且版本相同 → 直接进终端
                if (remoteVersion == localVersion && scriptFile.exists()) {
                    _uiState.update {
                        it.copy(
                            phase = ScreenPhase.Terminal,
                            downloadState = DownloadState.Success,
                            remoteVersion = remoteVersion,
                            remoteChangelog = changelog,
                            scriptPath = scriptFile.absolutePath,
                        )
                    }
                } else {
                    // 缓存下载 URL
                    cachedDownloadUrl = downloadUrl
                    // 显示更新弹窗
                    _uiState.update {
                        it.copy(
                            phase = ScreenPhase.ShowUpdate,
                            downloadState = DownloadState.Idle,
                            remoteVersion = remoteVersion,
                            remoteSha256 = remoteSha256,
                            remoteChangelog = changelog,
                            localVersion = localVersion,
                        )
                    }
                }
            } catch (e: Exception) {
                // 检查失败但本地有脚本 → 直接进终端
                if (scriptFile.exists()) {
                    _uiState.update { it.copy(phase = ScreenPhase.Terminal, downloadState = DownloadState.Success) }
                } else {
                    _uiState.update {
                        it.copy(phase = ScreenPhase.Idle, downloadState = DownloadState.Failed("${e.javaClass.simpleName}: ${e.message}"))
                    }
                }
            }
        }
    }

    /** Step 2: 用户在弹窗点"下载" → 下载脚本 */
    fun startDownload() {
        val url = cachedDownloadUrl
        val version = _uiState.value.remoteVersion
        val sha256 = _uiState.value.remoteSha256
        viewModelScope.launch { downloadScript(url, version, sha256) }
    }

    /** 直接进终端 (跳过更新检查) */
    fun enterTerminal() {
        if (scriptFile.exists()) {
            _uiState.update { it.copy(phase = ScreenPhase.Terminal, scriptPath = scriptFile.absolutePath) }
        } else {
            checkVersion() // 没脚本就回退到检查更新流程
        }
    }

    fun dismissDialog() {
        // 本地有脚本 → 进终端
        if (scriptFile.exists()) {
            _uiState.update { it.copy(phase = ScreenPhase.Terminal, scriptPath = scriptFile.absolutePath) }
        } else {
            _uiState.update { it.copy(phase = ScreenPhase.Idle) }
        }
    }

    // 缓存下载 URL (不暴露给 UI)
    private var cachedDownloadUrl: String = ""

    private suspend fun downloadScript(url: String, version: String, sha256: String) {
        _uiState.update { it.copy(downloadState = DownloadState.Downloading, downloadProgress = 0f) }
        try {
            withContext(Dispatchers.IO) {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 30_000
                conn.readTimeout = 120_000
                val totalSize = conn.contentLengthLong

                val tempFile = File(scriptDir, "${SCRIPT_NAME}.tmp")
                tempFile.delete() // 清理旧残留

                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { fos ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (totalSize > 0) {
                                _uiState.update {
                                    it.copy(downloadProgress = totalRead.toFloat() / totalSize)
                                }
                            }
                        }
                    }
                }
                conn.disconnect()

                // SHA256 校验 (如果 version.json 提供了)
                if (sha256.isNotEmpty()) {
                    _uiState.update { it.copy(downloadState = DownloadState.Verifying) }
                    val actual = sha256(tempFile)
                    if (!actual.equals(sha256, ignoreCase = true)) {
                        tempFile.delete()
                        _uiState.update {
                            it.copy(downloadState = DownloadState.Failed(
                                "SHA256 校验失败！期望: $sha256\n实际: $actual"))
                        }
                        return@withContext
                    }
                }

                // 原子替换
                val finalFile = scriptFile
                if (finalFile.exists()) finalFile.delete()
                tempFile.renameTo(finalFile)
                finalFile.setExecutable(true)
                saveLocalVersion(version)
            }

            _uiState.update {
                it.copy(
                    phase = ScreenPhase.Terminal,
                    downloadState = DownloadState.Success,
                    scriptPath = scriptFile.absolutePath,
                    localVersion = version,
                    downloadProgress = 1f,
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    downloadState = DownloadState.Failed("${e.javaClass.simpleName}: ${e.message}"),
                    downloadProgress = 0f,
                )
            }
        }
    }

    fun setTerminalRunning(running: Boolean) {
        _uiState.update { it.copy(isTerminalRunning = running) }
    }

    fun setExitCode(code: Int) {
        _uiState.update { it.copy(terminalExitCode = code, isTerminalRunning = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(downloadState = DownloadState.Idle) }
    }

    fun localVersionDisplay(): String = readLocalVersion()

    private fun downloadText(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000; conn.readTimeout = 15_000
        return conn.inputStream.bufferedReader().use { it.readText() }
            .also { conn.disconnect() }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun saveLocalVersion(v: String) {
        File(scriptDir, "version.json").writeText("""{"version":"$v"}""")
    }

    private fun readLocalVersion(): String {
        val f = File(scriptDir, "version.json")
        return if (f.exists()) {
            try {
                JSONObject(f.readText()).optString("version", "")
            } catch (_: Exception) { "" }
        } else ""
    }
}
