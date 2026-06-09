package com.zayu.mizu.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.navigation3.Navigator
import com.zayu.mizu.ui.navigation3.Route
import com.zayu.mizu.ui.util.getSuSFSStatus
import com.zayu.mizu.ui.util.rememberKpmAvailable
import com.zayu.mizu.ui.viewmodel.SettingsViewModel

@Composable
fun SettingPager(
    navigator: Navigator,
    bottomInnerPadding: Dp
) {
    val context = LocalContext.current
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isKpmAvailable = rememberKpmAvailable()
    val isSusfsSupported = getSuSFSStatus().equals("true", ignoreCase = true)

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val actions = SettingsScreenActions(
        onSetCheckUpdate = viewModel::setCheckUpdate,
        onSetCheckModuleUpdate = viewModel::setCheckModuleUpdate,
        onOpenTheme = { navigator.push(Route.ColorPalette) },
        onSetUiModeIndex = { index ->
            viewModel.setUiMode(if (index == 0) UiMode.Miuix.value else UiMode.Material.value)
        },
        onOpenProfileTemplate = { navigator.push(Route.AppProfileTemplate) },
        onSetSuCompatMode = viewModel::setSuCompatMode,
        onSetKernelUmountEnabled = viewModel::setKernelUmountEnabled,
        onSetSelinuxHideEnabled = viewModel::setSelinuxHideEnabled,
        onSetSulogEnabled = viewModel::setSulogEnabled,
        onSetAdbRootEnabled = viewModel::setAdbRootEnabled,
        onSetDefaultUmountModules = viewModel::setDefaultUmountModules,
        onSetEnableWebDebugging = viewModel::setEnableWebDebugging,
        onSetAutoJailbreak = viewModel::setAutoJailbreak,
        onSetCheckVersionMatch = viewModel::setCheckVersionMatch,
        onSetEnableSoundEffect = viewModel::setEnableSoundEffect,
        onSetIconStyle = { style -> viewModel.setIconStyle(context, style) },
        onOpenCustomIcon = { navigator.push(Route.CustomIcon) },
        onOpenAbout = { navigator.push(Route.About) },
        onSetAlternativeIcon = { enabled -> viewModel.setAlternativeIcon(context, enabled) },
        onOpenTools = { navigator.push(Route.Tool) },
        onOpenKpm = { navigator.push(Route.Kpm) },
        onOpenSusfsConfig = { navigator.push(Route.SuSFS) },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingPagerMiuix(
            uiState = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
            isKpmAvailable = isKpmAvailable,
            isSusfsSupported = isSusfsSupported
        )
        UiMode.Material -> SettingPagerMaterial(
            uiState = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
            isKpmAvailable = isKpmAvailable,
            isSusfsSupported = isSusfsSupported
        )
    }
}
