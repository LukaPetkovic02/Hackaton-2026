package com.example.myapplication.ui.theme

import android.app.Activity
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
    primary = FiscalAqua,
    secondary = FiscalBlue,
    tertiary = FiscalAqua,
    background = FiscalBlueDark,
    surface = Color(0xFF14466D),
    onPrimary = FiscalMist,
    onSecondary = FiscalMist,
    onBackground = FiscalMist,
    onSurface = FiscalMist,
    outline = Color(0xFF8FB9D3)
)

private val LightColorScheme = lightColorScheme(
    primary = FiscalAqua,
    secondary = FiscalBlue,
    tertiary = FiscalAqua,
    background = Color(0xFF0D355A),
    surface = Color(0xFF14466D),
    onPrimary = FiscalBlueDark,
    onSecondary = FiscalMist,
    onBackground = FiscalMist,
    onSurface = FiscalMist,
    outline = Color(0xFF9CC7E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep a stable fiscal palette.
    dynamicColor: Boolean = false,
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
