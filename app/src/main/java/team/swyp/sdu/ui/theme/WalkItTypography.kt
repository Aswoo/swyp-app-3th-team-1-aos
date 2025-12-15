package team.swyp.sdu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

val WalkItTypography = Typography(

    // ========== Heading ==========
    displayLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.HeadingXL,
        lineHeight = TypeScale.HeadingXL * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.SemiBold
    ),

    displayMedium = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.HeadingL,
        lineHeight = TypeScale.HeadingL * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.SemiBold
    ),

    displaySmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.HeadingM,
        lineHeight = TypeScale.HeadingM * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Medium
    ),

    headlineSmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.HeadingS,
        lineHeight = TypeScale.HeadingS * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Medium
    ),

    // ========== Body ==========
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.BodyXL,
        lineHeight = TypeScale.BodyXL * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Medium
    ),

    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.BodyL,
        lineHeight = TypeScale.BodyL * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Normal
    ),

    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.BodyM,
        lineHeight = TypeScale.BodyM * 1.5f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Normal
    ),

    // ========== Caption ==========
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = TypeScale.CaptionM,
        lineHeight = TypeScale.CaptionM * 1.3f,
        letterSpacing = TypeScale.LetterSpacing,
        fontWeight = FontWeight.Normal
    )
)
