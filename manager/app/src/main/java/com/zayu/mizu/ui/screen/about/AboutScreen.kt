package com.zayu.mizu.ui.screen.about

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import com.zayu.mizu.BuildConfig
import com.zayu.mizu.R
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import com.zayu.mizu.ui.navigation3.LocalNavigator

@Composable
fun AboutScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val htmlString = stringResource(
        id = R.string.about_source_code,
        "<b><a href=\"placeholder://github\">GitHub</a></b>",
        "<b><a href=\"https://t.me/KernelSU\">Telegram</a></b> 加入原KernelSU群组",
        "<b>怡子曰曰</b>",
        "<b>明风 OuO</b>",
        "<b><a href=\"https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt\">CC BY-NC-SA 4.0</a></b>"
    )
    val state = AboutUiState(
        title = stringResource(R.string.about),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        links = extractLinks(htmlString),
        acknowledgments = "感谢 KernelSU 近乎完美的开源生态体系。从内核模块到用户空间管理器，再到驱动级兼容方案，KernelSU 以精湛的架构设计和严谨的代码规范，为 Android Root 方案树立了全新标杆。向 KernelSU 开源社区无私的共产主义精神致以最崇高的敬意！\n\n向 KernelSU 核心作者 tiann 同志、weishu 同志以及全体贡献者致意！是你们开创了内核级 Root 方案的新纪元，让无数设备摆脱了传统 Root 方案的桎梏，获得了真正的自由与安全。\n\n特别声明：本 MizuSU 管理器分支仅对管理器 APP（MizuSU）UI 进行美化改动，所有底层生态均完全遵循 KernelSU LKM 驱动走向，不做任何内核级别的修改。您所使用的一切 Root 功能均来自 KernelSU 官方驱动，安全可靠。\n\n本管理器由古法编程创作辅助与 AI Agent 开发生态共同创作完成。开发过程之所以如此顺利且迅速，完全得益于 KernelSU 代码库的高度模块化、公开透明的文档体系，以及当今 AI 辅助编程时代的迅猛发展。\n\nMizuSU 由酷安 @民間の人民の利益を取る 进行设想与创作，旨在弥补 KernelSU 在个性化自定义方面的空缺 Σ(ﾟ∀ﾟﾉ)ﾉ（在此也向隔壁 FolkPatch 等类似分支项目致意）。从图标美化到主题切换，从自定义快捷方式到细节动画，每项功能都围绕让用户的管理器更个性这一初心展开。\n\n由于本项目纯属一时兴起的尝试，未来发展方向和维护周期尚不确定。本杂鱼的鱼粮全部由群内好心鱼鱼们赞助动力，在此对每一位群成员和志愿者表示由衷的感谢！\n\n愿开源精神永存。杂鱼~杂鱼~♪",
    )
    val actions = AboutScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onOpenLink = { url ->
            if (url.startsWith("placeholder://")) {
                Toast.makeText(context, "暂未建立开源库，敬请期待", Toast.LENGTH_SHORT).show()
            } else {
                uriHandler.openUri(url)
            }
        },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutScreenMiuix(state, actions)
        UiMode.Material -> AboutScreenMaterial(state, actions)
        UiMode.MizuSU -> AboutScreenMaterial(state, actions)
    }
}
