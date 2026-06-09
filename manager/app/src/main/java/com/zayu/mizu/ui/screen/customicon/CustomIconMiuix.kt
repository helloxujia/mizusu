package com.zayu.mizu.ui.screen.customicon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zayu.mizu.R
import com.zayu.mizu.ui.theme.LocalEnableBlur
import com.zayu.mizu.ui.util.BlurredBar
import com.zayu.mizu.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun CustomIconMiuix(
    iconStyle: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

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
            BlurredBar(backdrop) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "选择一个图标样式",
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(icons) { index, preset ->
                    val selected = iconStyle == index
                    val scale by animateFloatAsState(if (selected) 1.05f else 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelect(index) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    clip = true
                                    shape = CircleShape
                                }
                                .then(
                                    if (selected)
                                        Modifier.border(3.dp, colorScheme.primary, CircleShape)
                                    else Modifier.border(1.dp, colorScheme.onBackground.copy(alpha = 0.3f), CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(preset.previewRes),
                                contentDescription = preset.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            preset.name,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .border(1.dp, colorScheme.onBackground.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Filled.Add),
                                contentDescription = "自定义上传",
                                tint = colorScheme.onBackground,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "自定义",
                            textAlign = TextAlign.Center,
                            color = colorScheme.onBackground,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
