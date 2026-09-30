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
    primary = ElegantLavender,
    onPrimary = ElegantLavenderOnPrimary,
    primaryContainer = ElegantLavenderDark,
    onPrimaryContainer = ElegantLavenderLight,
    secondary = ElegantSecondary,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = ElegantSecondaryDark,
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = ElegantSafeGreen,
    onTertiary = Color(0xFF00390A),
    tertiaryContainer = ElegantSafeGreenContainer,
    onTertiaryContainer = ElegantSafeGreenLight,
    error = ElegantThreatRed,
    onError = Color(0xFF601410),
    errorContainer = ElegantThreatRedContainer,
    onErrorContainer = ElegantThreatRedLight,
    background = ElegantDarkBackground,
    onBackground = TextPrimaryDark,
    surface = ElegantDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = ElegantDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyberLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = SafeGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = ThreatRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = CyberLightBackground,
    onBackground = TextPrimaryLight,
    surface = CyberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CyberLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = CyberLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional cybersecurity palette
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
