package com.zayu.mizu.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.zayu.mizu.ui.MainActivity

fun toggleLauncherIcon(context: Context, useAlt: Boolean) {
    val pm = context.packageManager
    val main = ComponentName(context, MainActivity::class.java.name)
    val alias = ComponentName(context, "${MainActivity::class.java.name}Alias")

    pm.setComponentEnabledSetting(
        if (useAlt) alias else main,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )

    pm.setComponentEnabledSetting(
        if (useAlt) main else alias,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun setLauncherIconStyle(context: Context, style: Int) {
    val pm = context.packageManager
    val aliases = listOf(
        null,  // 0 = default (MainActivity itself)
        "${MainActivity::class.java.name}Alias",
        "${MainActivity::class.java.name}Alias2",
        "${MainActivity::class.java.name}Alias3"
    )

    // Disable all
    pm.setComponentEnabledSetting(
        ComponentName(context, MainActivity::class.java.name),
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
    for (aliasName in aliases.filterNotNull()) {
        pm.setComponentEnabledSetting(
            ComponentName(context, aliasName),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    // Enable selected
    if (style == 0) {
        pm.setComponentEnabledSetting(
            ComponentName(context, MainActivity::class.java.name),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    } else {
        pm.setComponentEnabledSetting(
            ComponentName(context, aliases[style] ?: return),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}