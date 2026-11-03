/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Root theme for Expressive Files.
 *
 * Uses Material You dynamic color on Android 12+ when [dynamicColor] is enabled, and the seeded
 * fallback palette otherwise. All color roles come from [MaterialTheme.colorScheme].
 */
@Composable
fun ExpressiveFilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
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

    // Material 3 1.4.0 keeps MaterialExpressiveTheme and MotionScheme.expressive() internal, so
    // use the public MaterialTheme overload with the ExpressiveMotion spring helpers as fallback.
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content
    )
}

/** Light, non-dynamic theme wrapper for `@Preview` composables. */
@Composable
fun ExpressiveFilesPreview(content: @Composable () -> Unit) {
    ExpressiveFilesTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            content = content
        )
    }
}
