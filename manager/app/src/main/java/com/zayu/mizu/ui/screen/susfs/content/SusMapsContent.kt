package com.zayu.mizu.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.screen.susfs.content.miuix.SusMapsContentMiuix
import com.zayu.mizu.ui.screen.susfs.content.material.SusMapsContentMaterial

@Composable
fun SusMapsContent(
    susMaps: Set<String>,
    isLoading: Boolean,
    onAddSusMap: () -> Unit,
    onRemoveSusMap: (String) -> Unit,
    onEditSusMap: ((String) -> Unit)? = null,
    onReset: (() -> Unit)? = null
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SusMapsContentMiuix(
            susMaps = susMaps,
            isLoading = isLoading,
            onAddSusMap = onAddSusMap,
            onRemoveSusMap = onRemoveSusMap,
            onEditSusMap = onEditSusMap,
            onReset = onReset
        )
        UiMode.Material -> SusMapsContentMaterial(
            susMaps = susMaps,
            isLoading = isLoading,
            onAddSusMap = onAddSusMap,
            onRemoveSusMap = onRemoveSusMap,
            onEditSusMap = { onEditSusMap?.invoke(it) },
            onReset = { onReset?.invoke() }
        )
    }
}
