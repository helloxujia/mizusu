package com.zayu.mizu.ui.screen.fishtoolbox

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── ANSI 颜色映射 ──
private val ANSI_COLORS = mapOf(
    30 to 0xFFCCCCCC, 31 to 0xFFE06C75, 32 to 0xFF98C379, 33 to 0xFFE5C07B,
    34 to 0xFF61AFEF, 35 to 0xFFC678DD, 36 to 0xFF56B6C2, 37 to 0xFFE8E8E8,
    90 to 0xFF5C6370, 91 to 0xFFF44747, 92 to 0xFF5AF25A, 93 to 0xFFFFFF55,
    94 to 0xFF55AAFF, 95 to 0xFFFF55FF, 96 to 0xFF55FFFF, 97 to 0xFFFFFFFF,
)
private val ANSI_BG = mapOf(
    40 to 0xFF1A1A2E, 41 to 0xFFE06C75, 42 to 0xFF98C379, 43 to 0xFFE5C07B,
    44 to 0xFF61AFEF, 45 to 0xFFC678DD, 46 to 0xFF56B6C2, 47 to 0xFF404040,
)

private fun parseAnsiToAnnotated(text: String, baseColor: Color): AnnotatedString = buildAnnotatedString {
    var fg = baseColor; var bg = Color.Transparent; var bold = false; var i = 0
    while (i < text.length) {
        when {
            text[i] == '' && i + 1 < text.length && text[i + 1] == '[' -> {
                val end = text.indexOf('m', i + 2)
                if (end > 0) {
                    text.substring(i + 2, end).split(';').forEach { c ->
                        when (c.toIntOrNull()) {
                            0 -> { fg = baseColor; bg = Color.Transparent; bold = false }
                            1 -> bold = true
                            in ANSI_COLORS -> fg = Color(ANSI_COLORS[c.toInt()]!!)
                            in ANSI_BG -> bg = Color(ANSI_BG[c.toInt()]!!)
                        }
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> {
                val start = i
                while (i < text.length && text[i] != '') i++
                withStyle(SpanStyle(color = fg, background = bg,
                    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)) {
                    append(text.substring(start, i))
                }
            }
        }
    }
}

/**
 * 终端视图 — 任意按键直接发送到 Shell（模拟真实终端交互）。
 *
 * 交互原理：
 *   - BasicTextField 捕获 IME 输入
 *   - 每次 onValueChange 计算差分 → 直接写入 Shell stdin
 *   - Shell 的 echo 回显在输出区（无需本地回显）
 *   - 支持 read -n 1（单字符读取）和 select（行读取）
 */
@Composable
fun TerminalComposable(
    modifier: Modifier = Modifier,
    scriptPath: String?,
    onServiceRunning: ((FishTerminalService) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val outputLines = remember { mutableStateListOf<AnnotatedString>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var serviceRef by remember { mutableStateOf<FishTerminalService?>(null) }

    // 终端输入状态 — 用于捕获 IME 并直传 Shell
    var termInput by remember { mutableStateOf(TextFieldValue("")) }
    var prevLen by remember { mutableIntStateOf(0) }  // 跟踪已发送长度
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }

    val bgTerminal = Color(0xFF0D1117)
    val bgToolbar = Color(0xFF161B22)
    val textColor = Color(0xFFE6EDF3)
    val accentColor = Color(0xFF58A6FF)
    val borderColor = Color(0xFF30363D)

    var lineBuf by remember { mutableStateOf("") }

    // ── 启动终端 ──
    fun startTerminal() {
        if (isRunning) return
        isRunning = true; errorMessage = null; prevLen = 0; termInput = TextFieldValue("")
        try {
            val intent = Intent(context, FishTerminalService::class.java).apply {
                action = FishTerminalService.ACTION_START
                putExtra(FishTerminalService.EXTRA_SCRIPT_PATH, scriptPath)
                putExtra(FishTerminalService.EXTRA_CWD, context.filesDir.absolutePath + "/" + SCRIPT_SUBDIR)
            }
            context.startService(intent)
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    val svc = (binder as FishTerminalService.LocalBinder).getService()
                    serviceRef = svc
                    if (svc.initError != null) { errorMessage = svc.initError; isRunning = false; return }
                    svc.setOnOutput { chunk ->
                        lineBuf += chunk
                        while (true) {
                            val nl = lineBuf.indexOf('\n'); if (nl < 0) break
                            val line = lineBuf.substring(0, nl); lineBuf = lineBuf.substring(nl + 1)
                            outputLines.add(parseAnsiToAnnotated(line, textColor))
                        }
                    }
                    svc.setOnExit { code ->
                        outputLines.add(AnnotatedString("\n── 进程退出, 代码: $code ──"))
                        isRunning = false
                    }
                    onServiceRunning?.invoke(svc)
                }
                override fun onServiceDisconnected(name: ComponentName?) { serviceRef = null }
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) { errorMessage = "启动失败: ${e.message}"; isRunning = false }
    }

    LaunchedEffect(Unit) { startTerminal() }

    // 自动聚焦
    LaunchedEffect(isRunning) { if (isRunning) focusRequester.requestFocus() }

    // 自动滚动
    LaunchedEffect(outputLines.size) {
        if (autoScroll && outputLines.isNotEmpty()) listState.animateScrollToItem(outputLines.size - 1)
    }

    // ══ 核心：IME 字符直传 Shell ══
    // 每次输入变化 → 计算新增/删除的字符 → 发送到 Shell
    fun handleTermInput(newValue: TextFieldValue) {
        val newText = newValue.text
        val oldText = termInput.text
        termInput = newValue

        // 计算差分
        if (newText.length > oldText.length) {
            // 新增字符: 逐个发送 (支持 read -n 1)
            val added = newText.substring(oldText.length)
            added.forEach { ch ->
                serviceRef?.writeStdin(ch.toString())
            }
        } else if (newText.length < oldText.length) {
            // 删除字符: 发送退格
            val deleted = oldText.length - newText.length
            repeat(deleted) { serviceRef?.writeStdin("\b") }
        }
        prevLen = newText.length
    }

    // ── 错误 ──
    if (errorMessage != null) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.ErrorOutline, null, Modifier.size(48.dp), tint = Color(0xFFF85149))
            Spacer(Modifier.height(16.dp))
            Text("终端启动失败", color = Color(0xFFF85149), fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = Color(0xFF8B949E))
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { isRunning = false; outputLines.clear(); startTerminal() }) { Text("重试") }
        }
        return
    }

    // ── 主界面 ──
    // statusBarsPadding: 避开状态栏 (时间/WiFi/电量) 不被遮挡
    // imePadding: 键盘上推整个布局
    Column(Modifier.fillMaxSize().background(bgTerminal).statusBarsPadding().imePadding()) {

        // ═══ 顶部状态栏 ═══
        Surface(Modifier.fillMaxWidth(), color = bgToolbar, shadowElevation = 2.dp) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                // 返回键
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Surface(color = accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(">_", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = accentColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("终端 · 杂鱼工具箱", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
                    Text(scriptPath?.substringAfterLast("/") ?: "交互模式", fontSize = 10.sp, color = Color(0xFF8B949E))
                }
                Surface(Modifier.size(8.dp), shape = RoundedCornerShape(4.dp), color = if (isRunning) Color(0xFF3FB950) else Color(0xFFF85149)) {}
                Spacer(Modifier.width(6.dp))
                Text(if (isRunning) "运行" else "停止", fontSize = 11.sp, color = if (isRunning) Color(0xFF3FB950) else Color(0xFFF85149))
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { outputLines.clear(); startTerminal() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Refresh, "重启", tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { serviceRef?.stopShell(); isRunning = false }, modifier = Modifier.size(28.dp), enabled = isRunning) {
                    Icon(Icons.Filled.Stop, "停止", tint = if (isRunning) Color(0xFFF85149) else Color(0xFF30363D), modifier = Modifier.size(16.dp))
                }
            }
        }

        // ═══ 输出区 ═══
        Surface(
            Modifier.fillMaxWidth().weight(1f).padding(8.dp),
            color = bgTerminal, shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.5.dp, borderColor),
        ) {
            LazyColumn(Modifier.fillMaxSize().padding(10.dp), state = listState) {
                if (outputLines.isEmpty()) {
                    item {
                        Text("░ 等待脚本输出…", color = Color(0xFF484F58), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                    }
                }
                items(outputLines.size) { idx ->
                    val line = outputLines[idx]
                    if (line.text.isNotEmpty()) {
                        Text(line, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 15.sp), modifier = Modifier.padding(vertical = 1.dp))
                    } else Spacer(Modifier.height(15.sp.value.dp))
                }
            }
        }

        // 回到底部
        if (!autoScroll && outputLines.size > 20) {
            val scope = rememberCoroutineScope()
            SmallFloatingActionButton(
                onClick = { autoScroll = true; scope.launch { listState.animateScrollToItem(outputLines.size - 1) } },
                modifier = Modifier.align(Alignment.End).padding(end = 16.dp, bottom = 4.dp),
                containerColor = accentColor,
            ) { Icon(Icons.Filled.KeyboardArrowDown, "回到底部", tint = Color.White) }
        }

        // ═══ 键盘输入层 (透明, 直传 Shell) ═══
        Surface(Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = bgToolbar) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 终端提示符
                Text("$", color = accentColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))

                // 透明文本输入 — 捕获 IME 并逐字直传 Shell
                Box(Modifier.weight(1f)) {
                    BasicTextField(
                        value = termInput,
                        onValueChange = { handleTermInput(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                            color = accentColor.copy(alpha = 0.3f),  // 半透明, 不干扰输出
                        ),
                        cursorBrush = SolidColor(accentColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        keyboardActions = KeyboardActions(
                            onDone = { focusRequester.requestFocus() }  // 保持焦点
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (termInput.text.isEmpty() && !isFocused) {
                                    Text("点按开始输入…", color = Color(0xFF484F58), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                // 手动发送 Enter (可选)
                IconButton(
                    onClick = { serviceRef?.writeStdin("\n"); termInput = TextFieldValue("") },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "回车", tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
