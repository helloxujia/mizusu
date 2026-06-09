package com.zayu.mizu.ui.screen.customicon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zayu.mizu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIconMaterial(
    iconStyle: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    val icons = listOf(
        IconPreset("图标1", R.drawable.ic_launcher_foreground),
        IconPreset("图标2", R.drawable.ic_launcher_foreground_alt),
        IconPreset("图标3", R.drawable.ic_launcher_foreground_alt2),
        IconPreset("图标4", R.drawable.ic_launcher_foreground_alt3),
        IconPreset("图标5", R.drawable.ic_launcher_foreground_alt4),
        IconPreset("图标6", R.drawable.ic_launcher_foreground_alt5),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "选择管理器图标",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(icons) { index, preset ->
                    val selected = iconStyle == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = preset.previewRes),
                                contentDescription = preset.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                preset.name,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                if (selected) "✓ 当前" else " ",
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

data class IconPreset(
    val name: String,
    val previewRes: Int
)
