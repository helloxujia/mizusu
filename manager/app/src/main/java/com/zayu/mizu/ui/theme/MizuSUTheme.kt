package com.zayu.mizu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme
import com.zayu.mizu.ui.webui.MonetColorsProvider

// ── MizuSU Brand Palette ──────────────────────────────────────────────
// 粉白基底 + 玫瑰色点缀 + 蓝灰辅助 + 金色高亮
object MizuSUColors {
    val PinkBase      = Color(0xFFF0DEE5)
    val PinkLight     = Color(0xFFFFEDF3)
    val RosePrimary   = Color(0xFFDE9BAB)
    val RoseDark      = Color(0xFFC27A8C)
    val BlueGrey      = Color(0xFF82AEC0)
    val BlueGreyDark  = Color(0xFF5C8A9A)
    val GoldAccent    = Color(0xFFFDD835)
    val DeepBrown     = Color(0xFF3D2028)
    val SoftWhite     = Color(0xFFFAF5F7)
}

// ── MizuSU Shapes ──────────────────────────────────────────────────────
// 二次元风格: 大圆角、柔和
val MizuSUShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(18.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// ── MizuSU Typography ──────────────────────────────────────────────────
// 圆体优先, 无额外表情符装饰
val MizuSUTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 10.sp, lineHeight = 14.sp),
)

// ── MizuSU Color Schemes ───────────────────────────────────────────────
val MizuSULightColorScheme = lightColorScheme(
    primary            = MizuSUColors.RosePrimary,
    onPrimary          = Color.White,
    primaryContainer   = MizuSUColors.PinkLight,
    onPrimaryContainer = MizuSUColors.DeepBrown,
    secondary          = MizuSUColors.BlueGrey,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFD6EAF0),
    onSecondaryContainer = Color(0xFF1A3A44),
    tertiary           = MizuSUColors.GoldAccent,
    onTertiary         = Color(0xFF3D2E00),
    tertiaryContainer  = Color(0xFFFFF3C4),
    onTertiaryContainer = Color(0xFF4A3800),
    background         = MizuSUColors.PinkBase,
    onBackground       = MizuSUColors.DeepBrown,
    surface            = MizuSUColors.PinkLight,
    onSurface          = MizuSUColors.DeepBrown,
    surfaceVariant     = Color(0xFFF5E8EC),
    onSurfaceVariant   = Color(0xFF5C4048),
    outline            = Color(0xFFB89BA3),
    outlineVariant     = Color(0xFFDDC5CC),
    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
)

val MizuSUDarkColorScheme = darkColorScheme(
    primary            = MizuSUColors.RoseDark,
    onPrimary          = Color(0xFF3D0A18),
    primaryContainer   = Color(0xFF5C2A38),
    onPrimaryContainer = MizuSUColors.PinkLight,
    secondary          = MizuSUColors.BlueGreyDark,
    onSecondary        = Color(0xFF0A2A33),
    secondaryContainer = Color(0xFF2A4A55),
    onSecondaryContainer = Color(0xFFD6EAF0),
    tertiary           = Color(0xFFE6C200),
    onTertiary         = Color(0xFF3D2E00),
    background         = Color(0xFF1A1014),
    onBackground       = Color(0xFFF0DEE5),
    surface            = Color(0xFF24181C),
    onSurface          = Color(0xFFF0DEE5),
    surfaceVariant     = Color(0xFF3D282E),
    onSurfaceVariant   = Color(0xFFDDC5CC),
)

// ── Spring Animation Presets ───────────────────────────────────────────
// 果冻弹性动效, 避免生硬过渡
object MizSUSprings {
    /** 按钮点击弹性 */
    val buttonPress = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )
    /** 卡片入场弹性 */
    val cardEnter = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessLow,
    )
    /** 页面过渡 */
    val pageTransition = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessLow,
    )
}

// ── Glass Effect Configuration ─────────────────────────────────────────
// 毛玻璃参数, 可被用户自定义覆盖
object MizuSUGlassConfig {
    /** 默认模糊半径 (dp) */
    var blurRadius: Float = 16f
    /** 毛玻璃层透明度 */
    var tintAlpha: Float = 0.15f
    /** 是否在支持的设备上启用实时模糊 */
    var enableRealtimeBlur: Boolean = true
}

// ── Composition Locals ─────────────────────────────────────────────────
/** 当前是否启用 MizuSU 毛玻璃效果 */
val LocalMizuSUGlassEnabled = staticCompositionLocalOf { true }

// ── Theme Entry Point ──────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MizuSUKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark ||
            (appSettings.colorMode.isSystem && systemDarkTheme)

    // MizuSU 品牌色作为 seed, 同时支持用户自定义 key color
    val seedColor = if (appSettings.keyColor == 0) {
        MizuSUColors.RosePrimary  // 默认玫瑰粉
    } else {
        Color(appSettings.keyColor)
    }

    val colorScheme = rememberDynamicColorScheme(
        seedColor    = seedColor,
        isDark       = darkTheme,
        isAmoled     = appSettings.colorMode.isAmoled,
        style        = appSettings.paletteStyle,
        specVersion  = appSettings.colorSpec,
        primary      = MizuSULightColorScheme.primary,
        secondary    = MizuSULightColorScheme.secondary,
        tertiary     = MizuSULightColorScheme.tertiary,
        neutral      = MizuSULightColorScheme.surface,
        neutralVariant = MizuSULightColorScheme.surfaceVariant,
        error        = MizuSULightColorScheme.error,
    )

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes      = MizuSUShapes,
        typography  = MizuSUTypography,
        content     = {
            MonetColorsProvider.UpdateCss()
            content()
        }
    )
}
