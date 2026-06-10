package com.zayu.mizu.ui.screen.about

import androidx.compose.runtime.Immutable

@Immutable
data class AboutUiState(
    val title: String,
    val appName: String,
    val versionName: String,
    val links: List<LinkInfo>,
    val acknowledgments: String = "",
)

@Immutable
data class AboutScreenActions(
    val onBack: () -> Unit,
    val onOpenLink: (String) -> Unit,
)
