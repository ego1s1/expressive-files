/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies that the theme settings actually reach MaterialTheme.colorScheme. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun trueBlackTheme_usesBlackSurfaces() {
        composeRule.setContent {
            ExpressiveFilesTheme(
                darkTheme = true,
                dynamicColor = false,
                blackTheme = true
            ) {
                assertEquals(Color.Black, MaterialTheme.colorScheme.surface)
                assertEquals(Color.Black, MaterialTheme.colorScheme.surfaceContainerLowest)
            }
        }
    }

    @Test
    fun standardDarkTheme_keepsTonalSurface() {
        composeRule.setContent {
            ExpressiveFilesTheme(
                darkTheme = true,
                dynamicColor = false,
                blackTheme = false
            ) {
                assertEquals(ExpressiveDarkColorScheme.surface, MaterialTheme.colorScheme.surface)
            }
        }
    }
}
