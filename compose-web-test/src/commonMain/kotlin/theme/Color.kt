package composewebtest.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2563EB)
val PrimaryLight = Color(0xFF60BDFF)
val Background = Color(0xFFF9F9F9)
val TextDark = Color(0xFF3A3E47)
val TextGray = Color(0xFF5F687C)
val White = Color(0xFFFFFFFF)
val SplashGradientStart = Color(0xFF60BDFF)
val SplashGradientEnd = Color(0x0094E8FF)


// Main Button Gradient
val MainGradient = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.02f to Color(0xFF6092FF),
        0.36f to Color(0xFF2563EB),
        1.0f to Color(0xFF1551D3),
    ),
)
