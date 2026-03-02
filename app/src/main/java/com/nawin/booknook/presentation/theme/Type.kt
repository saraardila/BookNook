package com.nawin.booknook.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nawin.booknook.R

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

val Lato = FontFamily(
    Font(R.font.lato_light, FontWeight.Light),
    Font(R.font.lato_regular, FontWeight.Normal)
)

val BookNookTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
)
