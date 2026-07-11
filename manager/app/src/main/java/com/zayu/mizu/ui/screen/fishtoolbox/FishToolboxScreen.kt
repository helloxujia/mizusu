package com.zayu.mizu.ui.screen.fishtoolbox

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.File

/** 终端页面 — 纯终端，自带工具栏无需外部 TopAppBar */
@Composable
fun FishToolboxScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scriptPath = remember {
        val f = File(context.filesDir, "$SCRIPT_SUBDIR/$SCRIPT_NAME")
        if (f.exists()) f.absolutePath else null
    }
    Box(Modifier.fillMaxSize()) {
        TerminalComposable(Modifier.fillMaxSize(), scriptPath, onBack = onBack)
    }
}
