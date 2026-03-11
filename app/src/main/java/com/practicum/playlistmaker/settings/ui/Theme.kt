package com.practicum.playlistmaker.settings.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun PlaylistMakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
val YsDisplay = FontFamily(
    Font(R.font.ys_display_regular, FontWeight.Normal)
)

val YsDisplayMedium = FontFamily(
    Font(R.font.ys_display_medium, FontWeight.Medium)
)

val AppTypography = Typography(

    titleLarge = TextStyle(
        fontFamily = YsDisplayMedium,
        fontSize = 22.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = YsDisplay,
        fontSize = 16.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = YsDisplay,
        fontSize = 11.sp
    ),

    titleMedium = TextStyle(
        fontFamily = YsDisplayMedium,
        fontSize = 19.sp
    ),

    titleSmall = TextStyle(
        fontFamily = YsDisplayMedium,
        fontSize = 14.sp
    ),

    labelSmall = TextStyle(
        fontFamily = YsDisplay,
        fontSize = 12.sp
    )
)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val BlackLight = Color(0xFF1A1B22)
val TrackOff = Color(0xFFE6E8EB)
val LightSearchBar = Color(0xFFAEAFB4)
val BackgroundMain = Color(0xFF3772E7)

// ------------------------
// Светлая тема
// ------------------------
val LightColors = lightColorScheme(
    primary = BlackLight,                  // colorPrimary
    onPrimary = White,                     // colorOnPrimary
    primaryContainer = LightSearchBar,    // colorPrimaryFixed / colorPrimaryVariant
    onPrimaryContainer = BlackLight,      // colorOnPrimaryContainer
    secondary = White,                     // colorSecondary
    onSecondary = Black,                   // colorOnSecondary
    secondaryContainer = LightSearchBar,  // colorSecondaryVariant
    tertiary = BackgroundMain,             // colorTertiary
    onTertiary = BlackLight,               // colorOnTertiary
    background = White,                    // стандартный фон
    onBackground = TrackOff,               // colorOnBackground
    surface = White,                       // для карточек / поверхностей
    onSurface = Black,                       // colorOnSurface
    tertiaryContainer = BlackLight          // поисковик
)

// ------------------------
// Тёмная тема
// ------------------------
val DarkColors = darkColorScheme(
    primary = White,                        // colorPrimary
    onPrimary = BlackLight,                 // colorOnPrimary
    primaryContainer = White,               // colorPrimaryFixed / colorPrimaryVariant
    onPrimaryContainer = White,             // colorOnPrimaryContainer
    secondary = White,                       // colorSecondary
    onSecondary = Black,                     // colorOnSecondary
    secondaryContainer = BlackLight,             // colorSecondaryVariant
    tertiary = BackgroundMain,              // colorTertiary
    onTertiary = White,                      // colorOnTertiary
    background = BlackLight,                 // основной фон
    onBackground = White,                    // colorOnBackground
    surface = BlackLight,                    // поверхность
    onSurface = White,                         // colorOnSurface
    tertiaryContainer = BlackLight          // поисковик
)

