/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Mirrors [FileRowTest] for grid mode: selection must be exposed as semantics, not only as
 * a container color, and taps must reach the caller.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileGridTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val entries = listOf(
        FileEntryUi(path = "/tmp/report.txt", name = "report.txt", isSelected = true),
        FileEntryUi(path = "/tmp/photo.png", name = "photo.png")
    )

    @Test
    fun selectedTile_exposesSelectedSemantics() {
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                FileGrid(
                    entries = entries,
                    onEntryClick = {},
                    onEntryLongClick = {},
                    onEntryMenu = {}
                )
            }
        }

        composeRule.onNodeWithText("report.txt").assertIsSelected()
    }

    @Test
    fun clickingTile_invokesCallbackWithEntry() {
        var clicked: FileEntryUi? = null
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                FileGrid(
                    entries = entries,
                    onEntryClick = { clicked = it },
                    onEntryLongClick = {},
                    onEntryMenu = {}
                )
            }
        }

        composeRule.onNodeWithText("photo.png").performClick()
        assertEquals("/tmp/photo.png", clicked?.path)
        assertTrue(clicked?.isSelected == false)
    }
}
