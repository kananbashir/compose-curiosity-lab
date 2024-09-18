package com.example.compose_curiosity_lab.splitthebill

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * Created on 8/31/2024
 * @author Kanan Bashir
 */

fun Modifier.shadow(
    color: Color = Color.Black,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0f.dp,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) = this.then(
    modifier.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            val spreadPixel = spread.toPx()
            val leftPixel = (0f - spreadPixel) + offsetX.toPx()
            val topPixel = (0f - spreadPixel) + offsetY.toPx()
            val rightPixel = (this.size.width + spreadPixel)
            val bottomPixel = (this.size.height + spreadPixel)

            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter =
                    (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }

            val shadowColor = color.copy(alpha = color.alpha * alpha)
            frameworkPaint.color = shadowColor.toArgb()

            it.drawRoundRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint
            )
        }
    }
)

fun SnapshotStateList<TransactionItem>.getTotalTransactionAmount(): BigDecimal {
    var totalAmount = 0.0
    forEach { totalAmount += it.transactionAmount }
    return BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_EVEN)
}

suspend inline fun <T, V: AnimationVector> Animatable<T, V>.animateWithResult(
    targetOffset: T,
    animationSpec: AnimationSpec<T>,
    onAnimationEnd: () -> Unit
) {
    when (animateTo(targetOffset, animationSpec).endReason) {
        AnimationEndReason.Finished -> { onAnimationEnd() }
        else -> {}
    }
}

inline fun applyWhenAllNotNull(vararg values: Any?, action: () -> Unit) {
    if (values.all { it != null }) action()
}

fun Offset.toIntOffset(): IntOffset {
    return IntOffset(
        x = this.x.roundToInt(),
        y = this.y.roundToInt()
    )
}