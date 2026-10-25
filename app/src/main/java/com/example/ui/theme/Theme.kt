package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRedBright,
    onPrimary = PureWhite,
    primaryContainer = CrimsonRedDark,
    onPrimaryContainer = CrimsonLight,
    secondary = Navy600,
    onSecondary = PureWhite,
    secondaryContainer = Navy700,
    onSecondaryContainer = PureWhite,
    tertiary = GuardianGreen,
    onTertiary = PureWhite,
    background = Navy900,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = Navy100,
    outline = SlateGray,
    error = CrimsonRedBright,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRed,
    onPrimary = PureWhite,
    primaryContainer = CrimsonLight,
    onPrimaryContainer = CrimsonRedDark,
    secondary = Navy700,
    onSecondary = PureWhite,
    secondaryContainer = Navy100,
    onSecondaryContainer = Navy800,
    tertiary = GuardianGreenDark,
    onTertiary = PureWhite,
    background = OffWhite,
    onBackground = Navy900,
    surface = PureWhite,
    onSurface = Navy900,
    surfaceVariant = Navy50,
    onSurfaceVariant = Navy700,
    outline = SlateGrayLight,
    error = CrimsonRed,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent Navy-Red-Green safety branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
