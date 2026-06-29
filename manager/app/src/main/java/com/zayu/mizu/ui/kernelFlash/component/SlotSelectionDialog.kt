package com.zayu.mizu.ui.kernelFlash.component

import androidx.compose.runtime.Composable
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode

@Composable
fun SlotSelectionDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSlotSelected: (String) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SlotSelectionDialogMiuix(
            show = show,
            onDismiss = onDismiss,
            onSlotSelected = onSlotSelected
        )
        UiMode.Material -> SlotSelectionDialogMaterial(
            show = show,
            onDismiss = onDismiss,
            onSlotSelected = onSlotSelected
        )
        UiMode.MizuSU -> SlotSelectionDialogMaterial(
            show = show,
            onDismiss = onDismiss,
            onSlotSelected = onSlotSelected
        )
    }
}
