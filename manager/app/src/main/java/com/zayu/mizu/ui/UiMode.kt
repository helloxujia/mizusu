package com.zayu.mizu.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiMode(val value: String) {
    Miuix("miuix"),
    Material("material"),
    MizuSU("mizusu");

    companion object {
        fun fromValue(value: String): UiMode = when (value) {
            Material.value -> Material
            MizuSU.value -> MizuSU
            else -> Miuix
        }

        val DEFAULT_VALUE = Miuix.value
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.MizuSU }
