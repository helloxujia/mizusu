package com.zayu.mizu.ui.screen.customicon

import androidx.compose.runtime.Composable
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode

@Composable
fun CustomIconScreen(
    iconStyle: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onCustomUpload: () -> Unit = {},
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> CustomIconMiuix(iconStyle, onSelect, onBack, onCustomUpload)
        UiMode.Material -> CustomIconMaterial(iconStyle, onSelect, onBack, onCustomUpload)
    }
}
