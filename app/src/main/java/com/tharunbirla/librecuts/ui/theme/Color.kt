package com.tharunbirla.librecuts.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ── ProdlineAI editor palette ────────────────────────────────────────────────
// Dark-first video-editing scheme: near-black stage, elevated panels, a single
// electric-violet accent carried over from the legacy brand (#CEC0EC family)
// plus the signature coral used by selection handles (#FF2A6D).

// Core surfaces (stage → panel → elevated)
val StageBlack = Color(0xFF0B0B10)
val SurfaceDark = Color(0xFF121218)
val SurfaceElevated = Color(0xFF1A1A23)
val SurfaceHighest = Color(0xFF24242F)

// Accent system
val AccentViolet = Color(0xFFB9A7F4)
val AccentVioletDim = Color(0xFF8B7BC7)
val AccentOnViolet = Color(0xFF1D1440)
val AccentCoral = Color(0xFFFF2A6D)

// Text roles
val TextPrimaryLight = Color(0xFFF2F1F7)
val TextSecondary = Color(0xFFA8A6B8)
val TextDisabled = Color(0xFF5C5A6B)

// Utility
val OutlineSubtle = Color(0xFF2E2E3A)
val DangerRed = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF4ADE80)
val TimelineTrackBlue = Color(0xFF4C8DF6)
val TimelineAudioGreen = Color(0xFF34C77B)

// MD3 scheme mapping: fixed-dark for editor predictability across OEM theming.
val ProdlineDarkScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = AccentOnViolet,
    primaryContainer = AccentVioletDim,
    onPrimaryContainer = AccentOnViolet,
    secondary = TimelineTrackBlue,
    onSecondary = StageBlack,
    secondaryContainer = SurfaceHighest,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentCoral,
    onTertiary = StageBlack,
    background = StageBlack,
    onBackground = TextPrimaryLight,
    surface = SurfaceDark,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceDark,
    surfaceContainerLow = StageBlack,
    surfaceContainerHigh = SurfaceElevated,
    surfaceContainerHighest = SurfaceHighest,
    outline = OutlineSubtle,
    outlineVariant = OutlineSubtle,
    error = DangerRed,
    onError = StageBlack
)
