package com.rc.axiom.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rc.axiom.R

val Ndot57 = FontFamily(
    Font(R.font.ndot57_regular, FontWeight.Normal)
)

val LetteraMonoLL = FontFamily(
    Font(R.font.letteramonoll_regular, FontWeight.Normal)
)

val defaultTypography = Typography()
val customTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = Ndot57),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = Ndot57),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = Ndot57),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Ndot57),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Ndot57),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Ndot57),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = Ndot57),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = Ndot57),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = Ndot57),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = LetteraMonoLL),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = LetteraMonoLL),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = LetteraMonoLL),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = LetteraMonoLL),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = LetteraMonoLL),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = LetteraMonoLL),
)

val monoTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = Ndot57),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = Ndot57),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = Ndot57),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Ndot57),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Ndot57),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Ndot57),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = Ndot57),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = Ndot57),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = Ndot57),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = LetteraMonoLL),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = LetteraMonoLL),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = LetteraMonoLL),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = LetteraMonoLL),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = LetteraMonoLL),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = LetteraMonoLL),
)
