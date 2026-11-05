/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** App-level animation switch, combined with the system animator duration scale. */
val LocalAppMotionEnabled = staticCompositionLocalOf { true }

/**
 * Root theme for Expressive Files.
 *
 * Uses Material You dynamic color on Android 12+ when [dynamicColor] is enabled, and the seeded
 * fallback palette otherwise. All color roles come from [MaterialTheme.colorScheme]. Expressive
 * motion is provided by Material3 1.5, so built-in components and custom transitions share the
 * same spatial/effects specs.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveFilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    blackTheme: Boolean = false,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> ExpressiveDarkColorScheme
        else -> ExpressiveLightColorScheme
    }
    val colorScheme = if (darkTheme && blackTheme) {
        baseColorScheme.withBlackSurfaces()
    } else {
        baseColorScheme
    }

    CompositionLocalProvider(LocalAppMotionEnabled provides animationsEnabled) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = ExpressiveShapes,
            typography = ExpressiveTypography,
            content = content
        )
    }
}

private fun ColorScheme.withBlackSurfaces(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF222222),
    surfaceBright = Color(0xFF2A2A2A)
)

/** Light, non-dynamic theme wrapper for `@Preview` composables. */
@Composable
fun ExpressiveFilesPreview(content: @Composable () -> Unit) {
    ExpressiveFilesTheme(
        darkTheme = false,
        dynamicColor = false,
        blackTheme = false
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            content = content
        )
    }
}
