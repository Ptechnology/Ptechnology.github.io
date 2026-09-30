package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BibleGoldLight,
    onPrimary = BibleNavyDark,
    primaryContainer = BibleNavyPrimary,
    onPrimaryContainer = BibleGoldLight,
    secondary = BibleGoldAccent,
    onSecondary = Color.Black,
    tertiary = BibleOlive,
    background = BibleNavyDark,
    surface = BibleDarkSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = BibleDarkCard,
    outline = BibleDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BibleNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = BibleNavyDark,
    secondary = BibleGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = BibleGoldLight,
    onSecondaryContainer = BibleGoldDark,
    tertiary = BibleOlive,
    background = BibleParchmentBg,
    surface = BibleParchmentCard,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF3EFE6),
    outline = BibleParchmentBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent dignified biblical theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
