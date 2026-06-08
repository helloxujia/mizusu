package com.zayu.mizu.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val uiMode: UiMode,
)
