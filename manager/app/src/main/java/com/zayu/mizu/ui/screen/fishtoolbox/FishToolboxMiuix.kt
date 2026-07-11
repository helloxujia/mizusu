package com.zayu.mizu.ui.screen.fishtoolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayu.mizu.R
import top.yukonga.miuix.kmp.basic.*

@Composable
fun FishToolboxMiuix(
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
                title = "🐟 ${stringResource(R.string.fish_toolbox)}",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
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
                        Text("检查云端版本…")
                    }
                }
                uiState.hasScript -> LaunchedEffect(Unit) { onEnterTerminal() }
            }

            // ── 浮层弹窗 (ShowUpdate 阶段) ──
            if (uiState.phase == ScreenPhase.ShowUpdate) {
                MiuixUpdateOverlay(
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
private fun MiuixUpdateOverlay(
    remoteVersion: String, localVersion: String, changelog: String,
    hasLocalScript: Boolean, downloadState: DownloadState, downloadProgress: Float,
    onDownload: () -> Unit, onSkip: () -> Unit, onRetry: () -> Unit, onDismiss: () -> Unit,
) {
    val isDownloading = downloadState is DownloadState.Downloading

    // 半透明遮罩
    Box(
        Modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                if (!isDownloading) onDismiss()
            },
        contentAlignment = Alignment.Center,
    ) {
        // 弹窗卡片
        Card(
            Modifier.fillMaxWidth(0.85f).clip(RoundedCornerShape(24.dp)),
            insideMargin = PaddingValues(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🐟", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("杂鱼工具箱", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("v$remoteVersion", fontSize = 14.sp)

                if (localVersion.isNotEmpty()) {
                    Text("当前: v$localVersion", fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                }

                if (changelog.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("更新内容", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(changelog, fontSize = 12.sp)
                        }
                    }
                }

                if (isDownloading) {
                    Spacer(Modifier.height(12.dp))
                    Text("下载中… ${(downloadProgress * 100).toInt()}%")
                }

                if (downloadState is DownloadState.Failed) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Text(downloadState.message, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                    }
                    Card(onClick = onRetry) { Text("重试", modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)) }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    Modifier.fillMaxWidth(),
                    onClick = if (!isDownloading) onDownload else null,
                ) {
                    Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        if (!isDownloading) { Icon(Icons.Filled.Download, null); Spacer(Modifier.width(6.dp)) }
                        Text(
                            if (isDownloading) "下载中…"
                            else if (hasLocalScript && localVersion != remoteVersion) "更新到 v$remoteVersion"
                            else "下载 v$remoteVersion"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Card(onClick = if (hasLocalScript) onSkip else onDismiss) {
                    Text(if (hasLocalScript) "跳过，使用本地脚本" else "取消", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
