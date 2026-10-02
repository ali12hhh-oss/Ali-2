package com.tharunbirla.librecuts.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ProdlineAI editor theme. Video editors are dark-first products; the scheme is
 * fixed-dark for brand consistency (no dynamic color) so the stage, panels and
 * accent system stay predictable across devices and OEM theming.
 */

@Composable
fun ProdlineTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StageBlack.toArgb()
            window.navigationBarColor = SurfaceDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ProdlineDarkScheme,
        typography = Typography,
        shapes = ProdlineShapes,
        content = content
    )
}

/** Back-compat alias — existing call sites keep working during migration. */
@Composable
fun LibreCutsTheme(content: @Composable () -> Unit) = ProdlineTheme(content = content)
