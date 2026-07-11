package com.zayu.mizu.ui.screen.fishtoolbox

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zayu.mizu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishToolboxMaterial(
    uiState: FishToolboxUiState,
    onCheckVersion: () -> Unit,
    onDownload: () -> Unit,
    onBack: () -> Unit,
    onDismissDialog: () -> Unit,
    onEnterTerminal: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (uiState.phase == ScreenPhase.Idle) onCheckVersion()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐟 ${stringResource(R.string.fish_toolbox)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                },
            )
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {

            // ── 主内容 ──
            when {
                uiState.phase == ScreenPhase.Terminal -> TerminalComposable(Modifier.fillMaxSize(), uiState.scriptPath)
                uiState.phase == ScreenPhase.Checking -> {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(); Spacer(Modifier.height(16.dp))
                        Text("检查云端版本…", color = MaterialTheme.colorScheme.outline)
                    }
                }
                uiState.hasScript -> LaunchedEffect(Unit) { onEnterTerminal() }
            }

            // ── 浮层弹窗 (ShowUpdate 阶段) ──
            if (uiState.phase == ScreenPhase.ShowUpdate) {
                MaterialUpdateDialog(
                    remoteVersion = uiState.remoteVersion,
                    localVersion = uiState.localVersion,
                    changelog = uiState.remoteChangelog,
                    hasLocalScript = uiState.hasScript,
                    downloadState = uiState.downloadState,
                    downloadProgress = uiState.downloadProgress,
                    onDownload = onDownload,
                    onSkip = onDismissDialog,
                    onRetry = onCheckVersion,
                    onDismiss = onBack,
                )
            }
        }
    }
}

@Composable
private fun MaterialUpdateDialog(
    remoteVersion: String, localVersion: String, changelog: String,
    hasLocalScript: Boolean, downloadState: DownloadState, downloadProgress: Float,
    onDownload: () -> Unit, onSkip: () -> Unit, onRetry: () -> Unit, onDismiss: () -> Unit,
) {
    val isDownloading = downloadState is DownloadState.Downloading

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        icon = { Text("🐟", style = MaterialTheme.typography.displaySmall) },
        title = { Text("杂鱼工具箱 v$remoteVersion") },
        text = {
            Column {
                if (localVersion.isNotEmpty()) {
                    Text("当前版本: v$localVersion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(8.dp))
                }
                if (changelog.isNotEmpty()) {
                    Text("更新内容", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Text(changelog, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (isDownloading) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("${(downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
                if (downloadState is DownloadState.Failed) {
                    Spacer(Modifier.height(8.dp))
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                        Text(downloadState.message, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        },
        confirmButton = {
            if (downloadState is DownloadState.Failed) {
                TextButton(onClick = onRetry) { Text("重试") }
            }
            Button(onClick = onDownload, enabled = !isDownloading) {
                if (isDownloading) { CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                Text(if (isDownloading) "下载中…" else if (hasLocalScript && localVersion != remoteVersion) "更新" else "下载")
            }
        },
        dismissButton = {
            if (hasLocalScript) TextButton(onClick = onSkip) { Text("跳过") }
            else TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
