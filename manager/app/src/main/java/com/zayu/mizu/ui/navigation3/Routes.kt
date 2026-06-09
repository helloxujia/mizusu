package com.zayu.mizu.ui.navigation3

import android.net.Uri
import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import com.zayu.mizu.ui.screen.flash.FlashIt
import com.zayu.mizu.ui.screen.modulerepo.RepoModuleArg
import com.zayu.mizu.ui.util.FlashItSerializer
import com.zayu.mizu.ui.util.RepoModuleArgSerializer
import com.zayu.mizu.ui.util.TemplateInfoSerializer
import com.zayu.mizu.ui.util.UriSerializer
import com.zayu.mizu.ui.viewmodel.TemplateViewModel

/**
 * Type-safe navigation keys for Navigation3.
 * Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
 */
sealed interface Route : NavKey, Parcelable {
    @Parcelize
    @Serializable
    data object Main : Route

    @Parcelize
    @Serializable
    data object Home : Route

    @Parcelize
    @Serializable
    data object SuperUser : Route

    @Parcelize
    @Serializable
    data object Module : Route

    @Parcelize
    @Serializable
    data object Settings : Route

    @Parcelize
    @Serializable
    data object About : Route

    @Parcelize
    @Serializable
    data object Sulog : Route

    @Parcelize
    @Serializable
    data object ColorPalette : Route

    @Parcelize
    @Serializable
    data object AppProfileTemplate : Route

    @Parcelize
    @Serializable
    data class TemplateEditor(
        @Serializable(with = TemplateInfoSerializer::class) val template: TemplateViewModel.TemplateInfo,
        val readOnly: Boolean
    ) : Route

    @Parcelize
    @Serializable
    data class AppProfile(val uid: Int) : Route

    @Parcelize
    @Serializable
    data class Install(@Serializable(with = UriSerializer::class) val preselectedKernelUri: Uri? = null) : Route

    @Parcelize
    @Serializable
    data class ModuleRepoDetail(@Serializable(with = RepoModuleArgSerializer::class) val module: RepoModuleArg) : Route

    @Parcelize
    @Serializable
    data object ModuleRepo : Route

    @Parcelize
    @Serializable
    data class Flash(@Serializable(with = FlashItSerializer::class) val flashIt: FlashIt) : Route

    @Parcelize
    @Serializable
    data class ExecuteModuleAction(val moduleId: String, val fromShortcut: Boolean = false) : Route

    @Parcelize
    @Serializable
    data class KernelFlash(@Serializable(with = UriSerializer::class) val kernelUri: Uri, val selectedSlot: String?, val kpmPatchEnabled: Boolean, val kpmUndoPatch: Boolean) : Route

    @Parcelize
    @Serializable
    data object Kpm: Route

    @Parcelize
    @Serializable
    data object SuSFS: Route

    @Parcelize
    @Serializable
    data object Tool: Route

    @Parcelize
    @Serializable
    data object UmountManager: Route

    @Parcelize
    @Serializable
    data object CustomIcon: Route
}
