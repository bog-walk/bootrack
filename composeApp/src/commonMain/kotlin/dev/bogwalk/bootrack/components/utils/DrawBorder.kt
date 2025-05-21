package dev.bogwalk.bootrack.components.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import dev.bogwalk.bootrack.style.*

internal fun DrawScope.drawGridSurface(lighter: Boolean = false) {
    val color = if (lighter) mediumGreen else darkGreen
    val spacing = 100f
    val lineStroke = 3f
    var xStart = spacing
    var yStart = spacing
    while (xStart < size.width) {
        drawLine(color, Offset(xStart, 0f), Offset(xStart, size.height), lineStroke)
        xStart += spacing
    }
    while (yStart < size.height) {
        drawLine(color, Offset(0f, yStart), Offset(size.width, yStart), lineStroke)
        yStart += spacing
    }
}

internal fun DrawScope.drawNavButtonBorder(
    isInFocus: Boolean
) {
    val tint = if (isInFocus) ghostWhite else lightGreen
    drawLine(
        tint,
        Offset(0f, 0f),
        Offset(0f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(0f, size.height),
        Offset(size.width * 0.2f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width, 0f),
        Offset(size.width, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width, size.height),
        Offset(size.width * 0.8f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
}

internal fun DrawScope.drawBorderWithShadow(
    lighter: Boolean = false,
    clippedCorners: Boolean = false
) {
    if (clippedCorners) {
        drawClippedBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt, true)
        drawClippedBorder(borderSmall.toPx(), 3.2.dp.toPx(), if (lighter) lightGreen else neonGreen, StrokeCap.Round, false)
    } else {
        drawBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt)
        drawBorder(borderSmall.toPx(), 3.2.dp.toPx(), if (lighter) lightGreen else neonGreen, StrokeCap.Round)
    }
}

internal fun DrawScope.drawSearchBorder() {
    val extra = 0f
    drawLine(lightGreen,
        Offset(size.width - extra, extra),
        Offset(size.width - extra, size.height - extra),
        borderSmall.toPx(), StrokeCap.Butt)
    drawLine(lightGreen,
        Offset(extra, extra),
        Offset(extra, size.height - extra),
        borderSmall.toPx(), StrokeCap.Butt)
    drawLine(lightGreen,
        Offset(extra, size.height - extra),
        Offset(size.width - extra, size.height - extra),
        borderSmall.toPx(), StrokeCap.Butt)
}

internal fun DrawScope.drawBorder(
    stroke: Float,
    extra: Float,
    color: Color,
    cap: StrokeCap
) {
    drawLine(color,
        Offset(extra, extra),
        Offset(size.width - extra, extra),
        stroke, cap)
    drawLine(color,
        Offset(size.width - extra, extra),
        Offset(size.width - extra, size.height - extra),
        stroke, cap)
    drawLine(color,
        Offset(extra, extra),
        Offset(extra, size.height - extra),
        stroke, cap)
    drawLine(color,
        Offset(extra, size.height - extra),
        Offset(size.width - extra, size.height - extra),
        stroke, cap)
}

internal fun DrawScope.drawClippedBorder(
    stroke: Float,
    extra: Float,
    color: Color,
    cap: StrokeCap,
    isShadow: Boolean
) {
    val clippedX = if (isShadow) extra + (size.width / 4) + 5f else extra + (size.width / 4)
    val clippedY = if (isShadow) extra + (size.height / 12) + 5f else extra + (size.height / 12)
    drawLine(color,
        Offset(extra, extra),
        Offset(size.width - clippedX, extra),
        stroke, cap)
    if (!isShadow) {
        drawLine(color,
            Offset(size.width - clippedX, extra),
            Offset(size.width - extra, clippedY),
            stroke, cap)
    }
    drawLine(color,
        Offset(size.width - extra, clippedY),
        Offset(size.width - extra, size.height - extra),
        stroke, cap)
    drawLine(color,
        Offset(extra, extra),
        Offset(extra, size.height - clippedY),
        stroke, cap)
    if (!isShadow) {
        drawLine(color,
            Offset(extra, size.height - clippedY),
            Offset(clippedX, size.height - extra),
            stroke, cap)
    }
    drawLine(color,
        Offset(clippedX, size.height - extra),
        Offset(size.width - extra, size.height - extra),
        stroke, cap)
}

internal fun DrawScope.drawSummaryCardBorder(
    isInFocus: Boolean
) {
    val tint = if (isInFocus) ghostWhite else lightGreen
    drawLine(
        tint,
        Offset(0f, 30f),
        Offset(0f, size.height - 30f),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(15f, 15f),
        Offset(15f, size.height - 15f),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(30f, 0f),
        Offset(30f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(30f, size.height),
        Offset(size.width * 0.1f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width, 30f),
        Offset(size.width, size.height - 30f),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width - 15f, 15f),
        Offset(size.width - 15f, size.height - 15f),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width - 30f, 0f),
        Offset(size.width - 30f, size.height),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
    drawLine(
        tint,
        Offset(size.width - 30f, 0f),
        Offset(size.width * 0.9f, 0f),
        borderSmall.toPx(),
        StrokeCap.Butt
    )
}

internal fun DrawScope.drawPaginationBorder(
    stroke: Float,
    extra: Float,
    color: Color,
    cap: StrokeCap
) {
    drawLine(color,
        Offset(extra, extra),
        Offset(extra, size.height - extra),
        stroke, cap)
    drawLine(color,
        Offset(size.width - extra, extra),
        Offset(size.width - extra, size.height - extra),
        stroke, cap)
}

internal fun DrawScope.drawDialogBorder() {
    drawBorder(borderSmall.toPx(), 0f, ghostWhite, StrokeCap.Butt)
    drawBorder(borderSmall.toPx(), 3.6f, lightGrey, StrokeCap.Round)
    drawBorder(borderSmall.toPx(), 6.3f, neonGreen, StrokeCap.Round)
}
