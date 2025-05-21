package dev.bogwalk.bootrack.style

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val BTTypography: Typography = Typography(
    titleLarge = TextStyle(
        color = ghostWhite,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        color = lightGrey,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
    ),
    bodyMedium = TextStyle(
        color = ghostWhite,
        fontSize = 16.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
    ),
    labelLarge = TextStyle(
        color = Color.Black,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        lineHeight = 20.sp,
    )
)
