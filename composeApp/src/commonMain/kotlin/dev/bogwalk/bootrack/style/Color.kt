package dev.bogwalk.bootrack.style

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

internal val surfaceBlack = Color(0xff121212)
internal val ghostWhite = Color(0xfff8f8ff)
internal val lightGrey = Color(0xff9a9a9e)

internal val neonGreen = Color(0xff7bf6a5)
internal val lightGreen = Color(0xff98fcbd)
internal val mediumGreen = Color(0xff335e45)
internal val darkGreen = Color(0xff1a3023)
internal val shadowGreen = Color(0xff103820)

internal val neonPink = Color(0xfff67bcc)
internal val lightPink = Color(0xfffc97d7)
internal val shadowPink = Color(0xff381c2f)

internal val neonBlue = Color(0xff7be2f6)
internal val shadowBlue = Color(0xff0e4e82)

internal val neonRed = Color(0xfff67b7b)
internal val neonOrange = Color(0xfffc98a5)

internal val neonYellow = Color(0xfff5e17a)
internal val shadowYellow = Color(0xff7b7502)

internal val BTColors = darkColorScheme(
    primary = neonGreen,
    onPrimary = Color.Black,
    secondary = neonPink,
    onSecondary = Color.Black,
    tertiary = neonBlue,
    onTertiary = Color.Black,
    background = surfaceBlack,
    surface = Color.Transparent, // this is necessary for desktop window design
    onSurface = ghostWhite,
    inverseSurface = ghostWhite,
    inverseOnSurface = Color.Black,
    error = neonRed,
    onError = Color.Black,
)
