package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.CalculatorColorTheme
import com.example.model.ThemeMode

fun getCalculatorColorScheme(
    themeMode: ThemeMode,
    colorTheme: CalculatorColorTheme,
    systemInDark: Boolean
): ColorScheme {
    val primary = Color(colorTheme.primaryHex)
    return when (themeMode) {
        ThemeMode.GLASS -> {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.40f),
                onPrimaryContainer = Color.White,
                secondary = Color(0xFF38BDF8),
                onSecondary = Color.Black,
                background = Color(0xFF070B14),
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0x381E293B),
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = Color(0x33334155),
                onSurfaceVariant = Color(0xFFCBD5E1),
                outlineVariant = Color(0x6694A3B8)
            )
        }
        ThemeMode.DARK -> {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.35f),
                onPrimaryContainer = Color.White,
                secondary = ZeroSpecialGold,
                onSecondary = Color.Black,
                background = ClevBgDark,
                onBackground = ClevTextLight,
                surface = ClevSurfaceDark,
                onSurface = ClevTextLight,
                surfaceVariant = Color(0xFF22293A),
                onSurfaceVariant = Color(0xFF94A3B8),
                outlineVariant = ClevBorderDark
            )
        }
        ThemeMode.LIGHT -> {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.15f),
                onPrimaryContainer = primary,
                secondary = Color(0xFF9E5C28),
                onSecondary = Color.White,
                background = ClevBgLight,            // Warm Cream "سكري"
                onBackground = ClevTextDark,         // Deep Fountain Pen Ink
                surface = ClevSurfaceLight,          // Warm Vellum / Stationery Surface
                onSurface = ClevTextDark,
                surfaceVariant = Color(0xFFEFE8DB),
                onSurfaceVariant = Color(0xFF6E685F),
                outlineVariant = ClevBorderLight
            )
        }
        ThemeMode.SYSTEM -> {
            if (systemInDark) {
                darkColorScheme(
                    primary = primary,
                    onPrimary = Color.White,
                    primaryContainer = primary.copy(alpha = 0.35f),
                    onPrimaryContainer = Color.White,
                    secondary = ZeroSpecialGold,
                    onSecondary = Color.Black,
                    background = ClevBgDark,
                    onBackground = ClevTextLight,
                    surface = ClevSurfaceDark,
                    onSurface = ClevTextLight,
                    surfaceVariant = Color(0xFF22293A),
                    onSurfaceVariant = Color(0xFF94A3B8),
                    outlineVariant = ClevBorderDark
                )
            } else {
                lightColorScheme(
                    primary = primary,
                    onPrimary = Color.White,
                    primaryContainer = primary.copy(alpha = 0.15f),
                    onPrimaryContainer = primary,
                    secondary = Color(0xFF9E5C28),
                    onSecondary = Color.White,
                    background = ClevBgLight,
                    onBackground = ClevTextDark,
                    surface = ClevSurfaceLight,
                    onSurface = ClevTextDark,
                    surfaceVariant = Color(0xFFEFE8DB),
                    onSurfaceVariant = Color(0xFF6E685F),
                    outlineVariant = ClevBorderLight
                )
            }
        }
    }
}

fun getCalculatorColorScheme(
    darkTheme: Boolean,
    colorTheme: CalculatorColorTheme
): ColorScheme = getCalculatorColorScheme(
    if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
    colorTheme,
    darkTheme
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorTheme: CalculatorColorTheme = CalculatorColorTheme.ROYAL_BLUE,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val colorScheme = getCalculatorColorScheme(themeMode, colorTheme, systemInDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
