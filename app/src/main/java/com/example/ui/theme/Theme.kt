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

private val HibretDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldSoft,
    secondary = AccentGold,
    onSecondary = Color.Black,
    secondaryContainer = AccentGoldDark,
    onSecondaryContainer = AccentGoldSoft,
    background = Color(0xFF121714),
    onBackground = Color(0xFFE5EBE6),
    surface = Color(0xFF19201C),
    onSurface = Color(0xFFE5EBE6),
    surfaceVariant = Color(0xFF232D27),
    onSurfaceVariant = Color(0xFFBDC7BF),
    outline = Color(0xFF3B4840),
    error = Color(0xFFE57373)
)

private val HibretLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldDark,
    secondary = AccentGold,
    onSecondary = Color.White,
    secondaryContainer = AccentGoldSoft,
    onSecondaryContainer = AccentGoldDark,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfacePure,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Hibret brand colors primary
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> HibretDarkColorScheme
        else -> HibretLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
