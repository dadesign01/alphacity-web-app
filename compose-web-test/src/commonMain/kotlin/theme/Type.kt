package composewebtest.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.pretendard_black
import composewebtest.generated.resources.pretendard_bold
import composewebtest.generated.resources.pretendard_extrabold
import composewebtest.generated.resources.pretendard_medium
import composewebtest.generated.resources.pretendard_regular
import composewebtest.generated.resources.pretendard_semibold
import org.jetbrains.compose.resources.Font


@Composable
fun Pretendard(): FontFamily {
    return FontFamily(
        Font(Res.font.pretendard_black, FontWeight.Black),
        Font(Res.font.pretendard_extrabold, FontWeight.ExtraBold),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
        Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
        Font(Res.font.pretendard_medium, FontWeight.Medium),
        Font(Res.font.pretendard_regular, FontWeight.Normal),
    )
}
@Composable
fun AlphaTypography(): Typography {
    val pretendard = Pretendard()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Black,
            fontSize = 45.sp,
            letterSpacing = (-0.45).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = (-0.66).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = (-0.54).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = (-0.6).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (-0.42).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = (-0.48).sp,
        ),
    )
}