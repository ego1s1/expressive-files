/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The create FAB menu starts collapsed, expands on FAB tap, and an item tap both fires
 * the callback and collapses the menu again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreateFabMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fabTap_expandsMenuAndItemClick_firesCallbackAndCollapses() {
        var createFolder = false
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                CreateFabMenu(
                    canPaste = false,
                    onCreateFolder = { createFolder = true },
                    onCreateFile = {},
                    onPaste = {}
                )
            }
        }

        composeRule.onNodeWithText("New folder").assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription("Create new item").performClick()
        composeRule.onNodeWithText("New folder").assertIsDisplayed()

        composeRule.onNodeWithText("New folder").performClick()
        assertTrue(createFolder)
        composeRule.onNodeWithText("New folder").assertIsNotDisplayed()
    }
}
