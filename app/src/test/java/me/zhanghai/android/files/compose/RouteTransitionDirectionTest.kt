/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pane transitions must mirror navigation direction: leaving Files slides forward,
 * coming home slides back, Search behaves as an overlay, and About is a child of Settings.
 */
class RouteTransitionDirectionTest {

    @Test
    fun leavingFiles_isForward() {
        assertTrue(isForwardRouteTransition("files", "locations"))
        assertTrue(isForwardRouteTransition("files", "settings"))
        assertTrue(isForwardRouteTransition("files", "search"))
    }

    @Test
    fun returningToFiles_isBackward() {
        assertFalse(isForwardRouteTransition("locations", "files"))
        assertFalse(isForwardRouteTransition("settings", "files"))
        assertFalse(isForwardRouteTransition("search", "files"))
    }

    @Test
    fun searchBehavesAsOverlay() {
        assertTrue(isForwardRouteTransition("files", "search"))
        assertFalse(isForwardRouteTransition("search", "files"))
    }

    @Test
    fun aboutIsChildOfSettings() {
        assertTrue(isForwardRouteTransition("settings", "about"))
        assertFalse(isForwardRouteTransition("about", "settings"))
    }

    @Test
    fun siblingOrder_isStable() {
        assertTrue(isForwardRouteTransition("locations", "settings"))
        assertFalse(isForwardRouteTransition("settings", "locations"))
        assertTrue(isForwardRouteTransition("locations", "locations"))
    }

    @Test
    fun settingsSubScreens_areChildrenOfSettings() {
        assertTrue(isForwardRouteTransition("settings", "settings/storages"))
        assertTrue(isForwardRouteTransition("settings", "settings/bookmarks"))
        assertTrue(isForwardRouteTransition("settings", "settings/standard-dirs"))
        assertFalse(isForwardRouteTransition("settings/storages", "settings"))
        assertFalse(isForwardRouteTransition("settings/bookmarks", "settings"))
        assertFalse(isForwardRouteTransition("settings/standard-dirs", "settings"))
        assertFalse(isForwardRouteTransition("settings/storages", "files"))
    }
}
