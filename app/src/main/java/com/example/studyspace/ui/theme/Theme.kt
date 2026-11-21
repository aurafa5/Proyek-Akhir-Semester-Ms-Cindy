package com.example.studyspace.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ====================================================
// StudySpace - Dark Color Scheme (Material 3)
// ====================================================
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = PrimaryText,
    primaryContainer = SecondarySurface,
    onPrimaryContainer = PrimaryText,
    secondary = AccentPurple,
    onSecondary = PrimaryText,
    secondaryContainer = SecondarySurface,
    onSecondaryContainer = PrimaryText,
    tertiary = SuccessGreen,
    onTertiary = Background,
    tertiaryContainer = SecondarySurface,
    onTertiaryContainer = SuccessGreen,
    background = Background,
    onBackground = PrimaryText,
    surface = Surface,
    onSurface = PrimaryText,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = SecondaryText,
    outline = CardBorder,
    outlineVariant = TertiaryText,
    error = ErrorRed,
    onError = PrimaryText,
    errorContainer = SecondarySurface,
    onErrorContainer = ErrorRed
)

@Composable
fun StudySpaceTheme(
    // StudySpace always uses dark theme
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
