/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Hand-built Material 3 palette derived from the Expressive Files seed color `#0B57D0`.
 *
 * These schemes are used when Material You dynamic color is unavailable (API < 31) or disabled.
 */
internal val ExpressiveLightColorScheme = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    inversePrimary = Color(0xFFADC6FF),
    secondary = Color(0xFF565E71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131C2B),
    tertiary = Color(0xFF6E5676),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF7D8FF),
    onTertiaryContainer = Color(0xFF271430),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF1A1B1F),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF1A1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    surfaceTint = Color(0xFF0B57D0),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF0F0F4),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF9F9FF),
    surfaceDim = Color(0xFFD9D9E0),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E8EE),
    surfaceContainerHighest = Color(0xFFE2E2E9),
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    primaryFixed = Color(0xFFD8E2FF),
    primaryFixedDim = Color(0xFFADC6FF),
    onPrimaryFixed = Color(0xFF001A41),
    onPrimaryFixedVariant = Color(0xFF004494),
    secondaryFixed = Color(0xFFDAE2F9),
    secondaryFixedDim = Color(0xFFBEC6DC),
    onSecondaryFixed = Color(0xFF131C2B),
    onSecondaryFixedVariant = Color(0xFF3E4759),
    tertiaryFixed = Color(0xFFF7D8FF),
    tertiaryFixedDim = Color(0xFFDABDE2),
    onTertiaryFixed = Color(0xFF271430),
    onTertiaryFixedVariant = Color(0xFF553E5D)
)

internal val ExpressiveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
    inversePrimary = Color(0xFF0B57D0),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283141),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDABDE2),
    onTertiary = Color(0xFF3D2846),
    tertiaryContainer = Color(0xFF553E5D),
    onTertiaryContainer = Color(0xFFF7D8FF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceTint = Color(0xFFADC6FF),
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF2F3033),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF37393E),
    surfaceDim = Color(0xFF111318),
    surfaceContainer = Color(0xFF1E1F25),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
    surfaceContainerLow = Color(0xFF1A1B1F),
    surfaceContainerLowest = Color(0xFF0C0E13),
    primaryFixed = Color(0xFFD8E2FF),
    primaryFixedDim = Color(0xFFADC6FF),
    onPrimaryFixed = Color(0xFF001A41),
    onPrimaryFixedVariant = Color(0xFF004494),
    secondaryFixed = Color(0xFFDAE2F9),
    secondaryFixedDim = Color(0xFFBEC6DC),
    onSecondaryFixed = Color(0xFF131C2B),
    onSecondaryFixedVariant = Color(0xFF3E4759),
    tertiaryFixed = Color(0xFFF7D8FF),
    tertiaryFixedDim = Color(0xFFDABDE2),
    onTertiaryFixed = Color(0xFF271430),
    onTertiaryFixedVariant = Color(0xFF553E5D)
)
