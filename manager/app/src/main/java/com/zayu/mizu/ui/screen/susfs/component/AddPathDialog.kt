package com.zayu.mizu.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.screen.susfs.component.miuix.AddPathDialogMiuix
import com.zayu.mizu.ui.screen.susfs.component.material.AddPathDialogMaterial

@Composable
fun AddPathDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    titleRes: Int,
    labelRes: Int,
    initialValue: String = ""
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> AddPathDialogMiuix(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            titleRes = titleRes,
            labelRes = labelRes,
            initialValue = initialValue
        )
        UiMode.Material -> AddPathDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            titleRes = titleRes,
            labelRes = labelRes,
            initialValue = initialValue
        )
    }
}
