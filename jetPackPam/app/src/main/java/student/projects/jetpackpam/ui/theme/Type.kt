package student.projects.jetpackpam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.LanguageViewModel

// Set of Material typography styles to start with

val SpaceGrotesk = FontFamily(
    Font(
        resId = R.font.space_grotesk_bold,
        weight = FontWeight.Bold
    )
)

val Inter = FontFamily(
    Font(
        resId = R.font.inter_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.inter_medium,
        weight = FontWeight.Medium
    )
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
)
@Composable
fun dynamicTypography(languageViewModel: LanguageViewModel): Typography {
    val fontSize = languageViewModel.fontSize.sp

    // Create a Typography instance using the user-selected font size
    return remember(fontSize) {
        Typography(
            bodyLarge = TextStyle(
                fontFamily = Inter,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                fontSize = fontSize,
                lineHeight = (fontSize.value * 1.5).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = Inter,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = (fontSize.value * 0.9).sp,
                lineHeight = (fontSize.value * 1.4).sp
            ),
            bodySmall = TextStyle(
                fontFamily = Inter,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                fontSize = (fontSize.value * 0.8).sp,
                lineHeight = (fontSize.value * 1.3).sp
            ),
            titleLarge = TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = (fontSize.value * 1.8).sp,
                lineHeight = (fontSize.value * 2).sp
            ),
            titleSmall = TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = (fontSize.value * 1.2).sp,
                lineHeight = (fontSize.value * 1.5).sp
            ),
        )
    }
}