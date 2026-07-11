package com.zayu.mizu.ui.screen.home

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayu.mizu.R
import com.zayu.mizu.magica.MagicaService
import com.zayu.mizu.ui.LocalMainPagerState
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.component.dialog.rememberLoadingDialog
import com.zayu.mizu.ui.navigation3.Navigator
import com.zayu.mizu.ui.navigation3.Route
import com.zayu.mizu.ui.screen.fishtoolbox.*
import com.zayu.mizu.ui.viewmodel.FishToolboxViewModel
import com.zayu.mizu.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun HomePager(
    navigator: Navigator,
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true
) {
    val viewModel = viewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState = LocalMainPagerState.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()

    var hasActivated by remember { mutableStateOf(false) }
    if (isCurrentPage) hasActivated = true

    if (hasActivated) {
        LaunchedEffect(Unit) { viewModel.refresh() }
    }

    // ── 工具箱弹窗状态 ──
    val fishVm = remember { FishToolboxViewModel(context.filesDir) }
    val fishState by fishVm.uiState.collectAsStateWithLifecycle()
    var showToolboxDialog by remember { mutableStateOf(false) }
    var fishClickLock by remember { mutableStateOf(false) }

    val actions = HomeActions(
        onInstallClick = { navigator.push(Route.Install()) },
        onSuperuserClick = { mainState.animateToPage(1) },
        onModuleClick = { mainState.animateToPage(2) },
        onOpenUrl = uriHandler::openUri,
        onDismissWelcome = {
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putBoolean("show_welcome", false).apply()
            viewModel.dismissWelcome()
        },
        onJailbreakClick = {
            loadingDialog.showLoading()
            context.startService(Intent(context, MagicaService::class.java))
            scope.launch(Dispatchers.IO) {
                delay(30_000)
                withContext(Dispatchers.Main) {
                    loadingDialog.hide()
                    Toast.makeText(context, R.string.jailbreak_timeout, Toast.LENGTH_LONG).show()
                }
            }
        },
        onFishToolboxClick = {
            if (!fishClickLock) {
                fishClickLock = true
                showToolboxDialog = true
            }
        },
    )

    // 弹窗关闭时释放锁
    if (!showToolboxDialog && fishClickLock) fishClickLock = false

    // 弹窗显示后自动检查版本
    LaunchedEffect(showToolboxDialog) {
        if (showToolboxDialog) fishVm.checkVersion()
    }

    when (LocalUiMode.current) {
        UiMode.Miuix -> HomePagerMiuix(state = uiState, actions = actions, bottomInnerPadding = bottomInnerPadding)
        UiMode.Material -> HomePagerMaterial(state = uiState, actions = actions, bottomInnerPadding = bottomInnerPadding)
        UiMode.MizuSU -> HomePagerMizuSU(state = uiState, actions = actions, bottomInnerPadding = bottomInnerPadding)
    }

    // ── 工具箱弹窗 (在主页上浮层) ──
    if (showToolboxDialog) {
        when (fishState.phase) {
            ScreenPhase.Checking -> { /* 加载中, checkVersion 已触发 */ }
            ScreenPhase.ShowUpdate -> {
                when (LocalUiMode.current) {
                    UiMode.Miuix -> FishToolboxDialogMiuix(
                        fishState = fishState,
                        onDownload = { fishVm.startDownload() },
                        onSkip = {
                            if (fishState.hasScript) { navigator.push(Route.FishToolbox); showToolboxDialog = false }
                            else showToolboxDialog = false
                        },
                        onDismiss = { showToolboxDialog = false },
                    )
                    else -> FishToolboxDialogMaterial(
                        fishState = fishState,
                        onDownload = { fishVm.startDownload() },
                        onSkip = {
                            if (fishState.hasScript) { navigator.push(Route.FishToolbox); showToolboxDialog = false }
                            else showToolboxDialog = false
                        },
                        onDismiss = { showToolboxDialog = false },
                        onRetry = { fishVm.checkVersion() },
                    )
                }
            }
            ScreenPhase.Terminal -> {
                LaunchedEffect(Unit) {
                    showToolboxDialog = false
                    navigator.push(Route.FishToolbox)
                }
            }
            ScreenPhase.Idle -> { /* no-op */ }
        }
    }
}

// ── Material 弹窗 ──
@Composable
fun FishToolboxDialogMaterial(
    fishState: FishToolboxUiState,
    onDownload: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val downloading = fishState.downloadState is DownloadState.Downloading
    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        icon = { Text("🐟", style = MaterialTheme.typography.displaySmall) },
        title = { Text("杂鱼工具箱 v${fishState.remoteVersion}") },
        text = {
            Column {
                if (fishState.localVersion.isNotEmpty()) {
                    Text("当前: v${fishState.localVersion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(8.dp))
                }
                if (fishState.remoteChangelog.isNotEmpty()) {
                    Text("更新内容", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Text(fishState.remoteChangelog, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (downloading) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { fishState.downloadProgress }, Modifier.fillMaxWidth())
                    Text("${(fishState.downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
                if (fishState.downloadState is DownloadState.Failed) {
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                        Text(fishState.downloadState.message, Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        },
        confirmButton = {
            if (fishState.downloadState is DownloadState.Failed) { TextButton(onClick = onRetry) { Text("重试") } }
            Button(onClick = onDownload, enabled = !downloading) {
                if (downloading) { CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                Text(if (downloading) "下载中…" else if (fishState.hasScript) "更新" else "下载")
            }
        },
        dismissButton = {
            if (fishState.hasScript) TextButton(onClick = onSkip) { Text("跳过") }
            else TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

// ── Miuix 弹窗 (使用 AlertDialog 保证在所有布局之上) ──
@Composable
fun FishToolboxDialogMiuix(
    fishState: FishToolboxUiState,
    onDownload: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    val downloading = fishState.downloadState is DownloadState.Downloading
    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        icon = { androidx.compose.material3.Text("🐟", style = MaterialTheme.typography.displaySmall) },
        title = { androidx.compose.material3.Text("杂鱼工具箱 v${fishState.remoteVersion}") },
        text = {
            Column {
                if (fishState.localVersion.isNotEmpty()) {
                    androidx.compose.material3.Text("当前: v${fishState.localVersion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(8.dp))
                }
                if (fishState.remoteChangelog.isNotEmpty()) {
                    androidx.compose.material3.Text("更新内容", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        androidx.compose.material3.Text(fishState.remoteChangelog, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (downloading) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { fishState.downloadProgress }, Modifier.fillMaxWidth())
                    androidx.compose.material3.Text("${(fishState.downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDownload, enabled = !downloading) {
                if (downloading) { CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                androidx.compose.material3.Text(if (downloading) "下载中…" else if (fishState.hasScript) "更新" else "下载")
            }
        },
        dismissButton = {
            if (fishState.hasScript) TextButton(onClick = onSkip) { androidx.compose.material3.Text("跳过") }
            else TextButton(onClick = onDismiss) { androidx.compose.material3.Text("取消") }
        },
    )
}
