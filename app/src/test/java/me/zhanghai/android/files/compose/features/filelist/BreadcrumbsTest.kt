/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.zhanghai.android.files.compose.model.BreadcrumbUi
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The breadcrumb rail must show the full trail including the current folder, with
 * ancestors clickable and the current crumb inert.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BreadcrumbsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val trail = listOf(
        BreadcrumbUi(path = "/storage", label = "Internal storage"),
        BreadcrumbUi(path = "/storage/Download", label = "Download"),
        BreadcrumbUi(
            path = "/storage/Download/FludPremium",
            label = "FludPremium",
            isCurrent = true
        )
    )

    @Test
    fun fullTrail_showsCurrentFolder() {
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                Breadcrumbs(breadcrumbs = trail, onBreadcrumbClick = {})
            }
        }

        composeRule.onNodeWithText("Internal storage").assertIsDisplayed()
        composeRule.onNodeWithText("Download").assertIsDisplayed()
        composeRule.onNodeWithText("FludPremium").assertIsDisplayed()
    }

    @Test
    fun clickingAncestor_invokesCallbackWithThatCrumb() {
        val clicked = mutableListOf<String>()
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                Breadcrumbs(
                    breadcrumbs = trail,
                    onBreadcrumbClick = { clicked.add(it.path) }
                )
            }
        }

        composeRule.onNodeWithText("Download").performClick()
        assertEquals(listOf("/storage/Download"), clicked)
    }

    @Test
    fun clickingCurrent_doesNotInvokeCallback() {
        val clicked = mutableListOf<String>()
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                Breadcrumbs(
                    breadcrumbs = trail,
                    onBreadcrumbClick = { clicked.add(it.path) }
                )
            }
        }

        composeRule.onNodeWithText("FludPremium").performClick()
        assertEquals(emptyList<String>(), clicked)
    }
}
