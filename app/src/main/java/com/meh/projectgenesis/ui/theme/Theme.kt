package com.meh.projectgenesis.ui.theme

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

private val GenesisDarkColorScheme = darkColorScheme(
    primary = GenesisCyanAccent,
    onPrimary = GenesisNavyDark,
    primaryContainer = GenesisCyanContainer,
    onPrimaryContainer = GenesisCyanOnContainer,
    secondary = GenesisCyanDim,
    onSecondary = GenesisNavyDark,
    secondaryContainer = GenesisIndigoDeep,
    onSecondaryContainer = GenesisCyanOnContainer,
    tertiary = Color(0xFF80D8FF),
    onTertiary = GenesisNavyDark,
    background = GenesisNavyDark,
    onBackground = Color(0xFFF1F5F9),
    surface = GenesisNavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = GenesisNavySurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = GenesisIndigoLight,
    outlineVariant = Color(0xFF334155),
    error = GenesisCritical,
    onError = Color.White
)

private val GenesisLightColorScheme = lightColorScheme(
    primary = GenesisLightPrimary,
    onPrimary = GenesisLightOnPrimary,
    primaryContainer = Color(0xFFD6E4F0),
    onPrimaryContainer = Color(0xFF072740),
    secondary = Color(0xFF0077B6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCAF0F8),
    onSecondaryContainer = Color(0xFF03045E),
    tertiary = Color(0xFF0096C7),
    onTertiary = Color.White,
    background = GenesisLightBg,
    onBackground = GenesisLightTextPrimary,
    surface = GenesisLightSurface,
    onSurface = GenesisLightTextPrimary,
    surfaceVariant = GenesisLightSurfaceVariant,
    onSurfaceVariant = GenesisLightTextSecondary,
    outline = GenesisLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = GenesisCritical,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Genesis identity strong by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GenesisDarkColorScheme
        else -> GenesisLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GenesisTypography,
        content = content
    )
}
