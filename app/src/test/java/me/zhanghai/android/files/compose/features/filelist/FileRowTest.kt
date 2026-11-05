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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the accessibility contract for a selected file row: selection must be exposed
 * as semantics, not only as a container color.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedRow_exposesSelectedSemantics() {
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                FileRow(
                    entry = FileEntryUi(
                        path = "/tmp/report.txt",
                        name = "report.txt",
                        supportingText = "2.1 kB",
                        isSelected = true
                    ),
                    onClick = {},
                    onLongClick = {},
                    onMenuClick = {}
                )
            }
        }

        composeRule.onNodeWithText("report.txt").assertIsSelected()
    }

    @Test
    fun clickingRow_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                FileRow(
                    entry = FileEntryUi(path = "/tmp/report.txt", name = "report.txt"),
                    onClick = { clicked = true },
                    onLongClick = {},
                    onMenuClick = {}
                )
            }
        }

        composeRule.onNodeWithText("report.txt").performClick()
        assertTrue(clicked)
    }
}
