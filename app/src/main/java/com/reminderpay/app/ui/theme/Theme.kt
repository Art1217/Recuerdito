package com.reminderpay.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Purple40,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary        = Teal40,
    onSecondary      = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF001F24),
    background       = BackgroundLight,
    surface          = SurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Purple80,
    onPrimary        = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary        = Teal80,
    background       = BackgroundDark,
)

@Composable
fun ReminderPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color supported on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ReminderPayTypography,
        content     = content
    )
}
