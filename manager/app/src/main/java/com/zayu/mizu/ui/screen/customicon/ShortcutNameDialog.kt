package com.zayu.mizu.ui.screen.customicon

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zayu.mizu.ui.LocalUiMode
import com.zayu.mizu.ui.UiMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class IconShape(val label: String) { Circle("圆形"), RoundedSquare("圆角方形") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutNameDialog(
    previewBitmap: Bitmap,
    onConfirm: (name: String, shape: IconShape) -> Unit,
    onDismiss: () -> Unit,
) {
    val isMiuix = LocalUiMode.current == UiMode.Miuix

    val surfaceColor: Color
    val onSurfaceColor: Color
    val onSurfaceVariantColor: Color
    val primaryColor: Color
    val outlineColor: Color
    val surfaceVariantColor: Color

    if (isMiuix) {
        val cs = MiuixTheme.colorScheme
        surfaceColor = cs.surfaceContainer
        onSurfaceColor = cs.onBackground
        onSurfaceVariantColor = cs.onBackground.copy(alpha = 0.5f)
        primaryColor = cs.primary
        outlineColor = cs.onBackground.copy(alpha = 0.15f)
        surfaceVariantColor = cs.surfaceContainerHigh
    } else {
        val cs = MaterialTheme.colorScheme
        surfaceColor = cs.surfaceContainerHigh
        onSurfaceColor = cs.onSurface
        onSurfaceVariantColor = cs.onSurfaceVariant
        primaryColor = cs.primary
        outlineColor = cs.outlineVariant
        surfaceVariantColor = cs.surfaceContainerHighest
    }

    var name by remember { mutableStateOf("MizuSU") }
    var selectedShape by remember { mutableStateOf(IconShape.Circle) }
    val imageBitmap = remember(previewBitmap) { previewBitmap.asImageBitmap() }

    val previewShape = when (selectedShape) {
        IconShape.Circle -> CircleShape
        IconShape.RoundedSquare -> RoundedCornerShape(16.dp)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = surfaceColor,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标预览（跟随选中样式）
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(previewShape)
                        .background(surfaceVariantColor)
                        .border(2.dp, outlineColor, previewShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "图标预览",
                        modifier = Modifier.fillMaxSize().clip(previewShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 样式选择器
                Text(
                    "图标样式",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconShape.entries.forEach { shape ->
                        val selected = selectedShape == shape
                        val thumbShape = when (shape) {
                            IconShape.Circle -> CircleShape
                            IconShape.RoundedSquare -> RoundedCornerShape(6.dp)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedShape = shape }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(thumbShape)
                                    .background(surfaceVariantColor)
                                    .then(
                                        if (selected)
                                            Modifier.border(2.5.dp, primaryColor, thumbShape)
                                        else Modifier.border(1.dp, outlineColor, thumbShape)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = shape.label,
                                    modifier = Modifier.fillMaxSize().clip(thumbShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                shape.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) primaryColor else onSurfaceVariantColor,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "创建桌面快捷方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )

                Text(
                    "自定义图标将固定到桌面",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("快捷方式名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = outlineColor,
                    )
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = onSurfaceVariantColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name.ifBlank { "MizuSU" }, selectedShape) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}
