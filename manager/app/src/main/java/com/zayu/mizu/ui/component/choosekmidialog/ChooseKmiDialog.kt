package com.zayu.mizu.ui.component.choosekmidialog

import androidx.compose.runtime.Composable
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode

@Composable
fun ChooseKmiDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> ChooseKmiDialogMiuix(show, onDismissRequest, onSelected)
        UiMode.Material -> ChooseKmiDialogMaterial(show, onDismissRequest, onSelected)
        UiMode.MizuSU -> ChooseKmiDialogMaterial(show, onDismissRequest, onSelected)
    }
}