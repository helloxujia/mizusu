package com.zayu.mizu.ui.screen.customicon

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun CropDialog(
    bitmap: Bitmap,
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // 裁剪框大小
    val cropSizePx = screenWidthPx * 0.7f
    val cropRadiusPx = cropSizePx * 0.15f

    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()

    // 初始缩放：让图片短边刚好填满裁剪框
    val baseScale = cropSizePx / minOf(imgW, imgH)

    // 居中偏移
    val centerX = screenWidthPx / 2f
    val centerY = screenHeightPx / 2f

    var scale by remember { mutableFloatStateOf(baseScale) }
    var offsetX by remember { mutableFloatStateOf(centerX - imgW * baseScale / 2f) }
    var offsetY by remember { mutableFloatStateOf(centerY - imgH * baseScale / 2f) }

    // 裁剪框边界
    val cropLeft = centerX - cropSizePx / 2f
    val cropTop = centerY - cropSizePx / 2f
    val cropRight = centerX + cropSizePx / 2f
    val cropBottom = centerY + cropSizePx / 2f

    val currentScale by rememberUpdatedState(scale)
    val currentOffsetX by rememberUpdatedState(offsetX)
    val currentOffsetY by rememberUpdatedState(offsetY)

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    // 基准显示尺寸（保持宽高比），graphicsLayer 在此基础上统一缩放
    val baseDisplayW = with(density) { (imgW * baseScale / density.density).dp }
    val baseDisplayH = with(density) { (imgH * baseScale / density.density).dp }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val oldScale = currentScale
                        val newScale = (oldScale * zoom).coerceIn(baseScale * 0.5f, baseScale * 3f)

                        // 裁剪框中心（屏幕坐标）
                        val cropCenterX = (cropLeft + cropRight) / 2f
                        val cropCenterY = (cropTop + cropBottom) / 2f

                        // 裁剪框中心对应的图像像素坐标（缩放前）
                        val focusImgX = (cropCenterX - currentOffsetX) / oldScale
                        val focusImgY = (cropCenterY - currentOffsetY) / oldScale

                        // 缩放后，让焦点仍位于裁剪框中心
                        var newX = cropCenterX - focusImgX * newScale + pan.x
                        var newY = cropCenterY - focusImgY * newScale + pan.y

                        val imgWDisplay = imgW * newScale
                        val imgHDisplay = imgH * newScale
                        if (imgWDisplay > cropSizePx) {
                            newX = newX.coerceIn(cropRight - imgWDisplay, cropLeft)
                        } else {
                            newX = cropLeft + (cropSizePx - imgWDisplay) / 2f
                        }
                        if (imgHDisplay > cropSizePx) {
                            newY = newY.coerceIn(cropBottom - imgHDisplay, cropTop)
                        } else {
                            newY = cropTop + (cropSizePx - imgHDisplay) / 2f
                        }

                        scale = newScale
                        offsetX = newX
                        offsetY = newY
                    }
                }
        ) {
            // 图片层 — 基准布局尺寸 + graphicsLayer 统一缩放，永不拉伸变形
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(baseDisplayW, baseDisplayH)
                    .graphicsLayer {
                        val factor = scale / baseScale
                        scaleX = factor
                        scaleY = factor
                        transformOrigin = TransformOrigin(0f, 0f)
                        clip = false
                    },
                contentScale = ContentScale.FillBounds
            )

            // 遮罩层 — 四块暗色矩形围出裁剪窗口
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maskColor = Color.Black.copy(alpha = 0.65f)
                // 上
                drawRect(maskColor, Offset(0f, 0f), Size(size.width, cropTop))
                // 下
                drawRect(maskColor, Offset(0f, cropBottom), Size(size.width, size.height - cropBottom))
                // 左
                drawRect(maskColor, Offset(0f, cropTop), Size(cropLeft, cropSizePx))
                // 右
                drawRect(maskColor, Offset(cropRight, cropTop), Size(size.width - cropRight, cropSizePx))

                // 裁剪框边框
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.6f),
                    topLeft = Offset(cropLeft, cropTop),
                    size = Size(cropSizePx, cropSizePx),
                    cornerRadius = CornerRadius(cropRadiusPx),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 提示文字
            Text(
                "拖动/缩放调整裁剪区域",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )

            // 按钮
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = Color.White)
                }
                TextButton(onClick = {
                    try {
                        val imgLeft = (cropLeft - offsetX) / scale
                        val imgTop = (cropTop - offsetY) / scale
                        val imgSize = cropSizePx / scale

                        val cx = imgLeft.roundToInt().coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
                        val cy = imgTop.roundToInt().coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
                        val side = imgSize.roundToInt().coerceIn(1, minOf(bitmap.width - cx, bitmap.height - cy).coerceAtLeast(1))

                        val square = Bitmap.createBitmap(bitmap, cx, cy, side, side)
                        val cropped = Bitmap.createScaledBitmap(square, 432, 432, true)
                        if (cropped !== square) square.recycle()
                        onConfirm(cropped)
                    } catch (_: Throwable) { onDismiss() }
                }) {
                    Text("确认", color = Color.White)
                }
            }
        }
    }
}
