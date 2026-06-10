package com.zayu.mizu.ui.screen.customicon

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zayu.mizu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIconMaterial(
    iconStyle: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onCustomUpload: () -> Unit = {},
    onRestore: () -> Unit = {},
    onToggleHide: () -> Unit = {},
) {
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
            TopAppBar(
                title = { Text("自定义图标") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item(span = { GridItemSpan(4) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        "选择图标",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "点击图标即可切换管理器桌面图标",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            itemsIndexed(icons) { index, preset ->
                val selected = iconStyle == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .then(
                                if (selected)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = preset.previewRes),
                            contentDescription = preset.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        preset.name,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCustomUpload() }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "自定义",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 隐藏桌面图标（研发中）──
            item(span = { GridItemSpan(4) }) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onToggleHide() }.padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("隐藏桌面图标", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("研发中，敬请期待", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = { onToggleHide() },
                        )
                    }
                }
            }

            item(span = { GridItemSpan(4) }) {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

data class IconPreset(
    val name: String,
    val previewRes: Int
)
