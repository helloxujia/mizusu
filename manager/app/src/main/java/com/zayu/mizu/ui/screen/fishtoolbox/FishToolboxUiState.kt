package com.zayu.mizu.ui.screen.fishtoolbox

import androidx.compose.runtime.Immutable

/**
 * 杂鱼工具箱 UI 状态模型。
 */
@Immutable
data class FishToolboxUiState(
    val phase: ScreenPhase = ScreenPhase.Idle,
    val downloadState: DownloadState = DownloadState.Idle,
    val remoteVersion: String = "",
    val localVersion: String = "",
    val scriptPath: String = "",
    val remoteSha256: String = "",
    val remoteChangelog: String = "",
    val downloadProgress: Float = 0f,
    val isTerminalRunning: Boolean = false,
    val terminalExitCode: Int? = null,
) {
    val hasScript: Boolean get() = scriptPath.isNotEmpty()
    val hasUpdate: Boolean get() =
        remoteVersion.isNotEmpty() && remoteVersion != localVersion
}

enum class ScreenPhase {
    Idle,       // 初始
    Checking,   // 检查云端版本
    ShowUpdate, // 显示更新弹窗 (有 changelog/版本信息)
    Terminal    // 进入终端
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data object Checking : DownloadState
    data object Downloading : DownloadState
    data object Verifying : DownloadState
    data object Success : DownloadState
    data class Failed(val message: String) : DownloadState
}

/** Gitee 仓库配置 */
const val VERSION_URL =
    "https://gitee.com/xujia2024/miscellaneous-fish-toolbox/raw/master/version.json"

/** 本地存储 */
const val SCRIPT_SUBDIR = "fish_toolbox"
const val SCRIPT_NAME = "杂鱼工具箱3.0.sh"
