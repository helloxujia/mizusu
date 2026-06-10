package com.zayu.mizu.ui.screen.customicon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zayu.mizu.R
import com.zayu.mizu.ui.theme.LocalEnableBlur
import com.zayu.mizu.ui.theme.LocalEnableFloatingBottomBarBlur
import com.zayu.mizu.ui.util.BlurredBar
import com.zayu.mizu.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.draw.drawBehind

@Composable
fun CustomIconMiuix(
    iconStyle: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onCustomUpload: () -> Unit = {},
    onRestore: () -> Unit = {},
    onToggleHide: () -> Unit = {},
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val barBlur = rememberBlurBackdrop(enableBlur)
    val barColor = if (barBlur != null) Color.Transparent else colorScheme.surface

    val enableGlass = LocalEnableFloatingBottomBarBlur.current

    val icons = listOf(
        IconPreset("默认", R.drawable.ic_launcher_foreground),
        IconPreset("风格2", R.drawable.ic_launcher_foreground_alt),
        IconPreset("风格3", R.drawable.ic_launcher_foreground_alt2),
        IconPreset("风格4", R.drawable.ic_launcher_foreground_alt3),
        IconPreset("风格5", R.drawable.ic_launcher_foreground_alt4),
        IconPreset("风格6", R.drawable.ic_launcher_foreground_alt5),
        IconPreset("风格7", R.drawable.ic_launcher_foreground_alt6),
        IconPreset("风格8", R.drawable.ic_launcher_foreground_alt7),
    )

    Scaffold(
        topBar = {
            BlurredBar(barBlur) {
                TopAppBar(
                    color = barColor,
                    title = "自定义图标",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                painter = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                                contentDescription = "返回"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        popupHost = { }
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── 标题区 ──
            item(span = { GridItemSpan(4) }) {
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        "选择图标",
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "点击图标即可切换管理器桌面图标",
                        color = colorScheme.onBackground.copy(alpha = 0.45f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // ── 预设图标卡片区 ──
            itemsIndexed(icons) { index, preset ->
                val selected = iconStyle == index

                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(index) }
                        .then(
                            if (enableGlass && !selected)
                                Modifier
                                    .border(0.5.dp, colorScheme.onBackground.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.defaultColors(
                        when {
                            enableGlass && selected -> colorScheme.primary.copy(alpha = 0.15f)
                            enableGlass -> colorScheme.surfaceContainer.copy(alpha = 0.45f)
                            selected -> colorScheme.primary.copy(alpha = 0.08f)
                            else -> colorScheme.surfaceContainer
                        },
                        Color.Transparent,
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .then(
                                    if (selected)
                                        Modifier
                                            .clip(CircleShape)
                                            .background(colorScheme.primary.copy(alpha = 0.12f))
                                            .border(2.5.dp, colorScheme.primary, CircleShape)
                                    else
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (enableGlass) colorScheme.onBackground.copy(alpha = 0.06f)
                                                else colorScheme.onBackground.copy(alpha = 0.04f)
                                            )
                                            .border(0.5.dp, colorScheme.onBackground.copy(alpha = 0.12f), CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(preset.previewRes),
                                contentDescription = preset.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(if (selected) 4.dp else 2.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            preset.name,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = if (selected) colorScheme.primary else colorScheme.onBackground,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            // ── 分割线 ──
            item(span = { GridItemSpan(4) }) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.5.dp,
                        color = colorScheme.onBackground.copy(alpha = 0.08f)
                    )
                    Text(
                        "自定义",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.5.dp,
                        color = colorScheme.onBackground.copy(alpha = 0.08f)
                    )
                }
            }

            // ── 自定义上传 ──
            item {
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCustomUpload() }
                        .then(
                            if (enableGlass)
                                Modifier.border(0.5.dp, colorScheme.onBackground.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.defaultColors(
                        if (enableGlass) colorScheme.surfaceContainer.copy(alpha = 0.45f)
                        else colorScheme.surfaceContainer,
                        Color.Transparent,
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        val dashColor = colorScheme.onBackground.copy(alpha = 0.18f)
                        val dashBg = if (enableGlass) colorScheme.onBackground.copy(alpha = 0.04f)
                            else colorScheme.onBackground.copy(alpha = 0.03f)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .drawBehind {
                                    val dashPath = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    drawCircle(
                                        color = dashColor,
                                        style = Stroke(width = 2.5f, pathEffect = dashPath)
                                    )
                                }
                                .clip(CircleShape)
                                .background(dashBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Filled.Add),
                                contentDescription = "自定义上传",
                                tint = colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "自定义",
                            textAlign = TextAlign.Center,
                            color = colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // ── 隐藏桌面图标（研发中）──
            item(span = { GridItemSpan(4) }) {
                Card(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onToggleHide() },
                    colors = CardDefaults.defaultColors(colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("隐藏桌面图标", color = colorScheme.onBackground)
                            Text("研发中，敬请期待", color = colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        androidx.compose.material3.Switch(
                            checked = false,
                            onCheckedChange = { onToggleHide() },
                        )
                    }
                }
            }

            // ── 底部留白 ──
            item(span = { GridItemSpan(4) }) {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
