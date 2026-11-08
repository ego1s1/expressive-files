/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import me.zhanghai.android.files.compose.model.BreadcrumbUi
import me.zhanghai.android.files.compose.model.DrawerEntryUi
import me.zhanghai.android.files.compose.model.DrawerSectionUi
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.model.FileListUiState
import me.zhanghai.android.files.compose.model.NavigationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Navigation-only contract tests for the shared UI models.
 *
 * [UiModelsTest] already covers [FileEntryUi], [FileListUiState] defaults/copies and
 * [me.zhanghai.android.files.compose.model.FileLayoutMode]. This file adds the drawer/route
 * ordering, breadcrumb and zero-selection invariants the pill and rail rely on. Everything here
 * is pure Kotlin: no Android runtime, no Context, no Compose runtime.
 */
class NavigationModelsTest {

    @Test
    fun drawerEntryUi_defaultsDescribeAPlainNavigableEntry() {
        val entry = DrawerEntryUi(
            id = "/storage/emulated/0/Download",
            title = "Downloads"
        )

        assertEquals("/storage/emulated/0/Download", entry.id)
        assertEquals("Downloads", entry.title)
        assertNull(entry.subtitle)
        assertEquals(0, entry.iconRes)
        assertFalse(entry.selected)
        assertNull(entry.badge)
        assertFalse(entry.isAction)
    }

    @Test
    fun drawerSectionUi_preservesEntryOrderAcrossCopy() {
        val section = DrawerSectionUi(
            id = "storages",
            title = "Storages",
            entries = listOf(
                DrawerEntryUi(id = "internal", title = "Internal storage"),
                DrawerEntryUi(id = "sdcard", title = "SD card"),
                DrawerEntryUi(id = "usb", title = "USB drive")
            )
        )

        assertEquals(
            listOf("internal", "sdcard", "usb"),
            section.entries.map { it.id }
        )

        val withoutSdCard = section.copy(
            entries = section.entries.filterNot { it.id == "sdcard" }
        )

        assertEquals(listOf("internal", "usb"), withoutSdCard.entries.map { it.id })
        assertEquals("storages", withoutSdCard.id)
        assertEquals("Storages", withoutSdCard.title)
        assertEquals(3, section.entries.size)
    }

    @Test
    fun drawerSectionUi_allowsAnUntitledEmptySection() {
        val section = DrawerSectionUi(id = "more")

        assertEquals("more", section.id)
        assertNull(section.title)
        assertTrue(section.entries.isEmpty())
    }

    @Test
    fun navigationUiState_defaultsDescribeAnEmptyShell() {
        val state = NavigationUiState()

        assertEquals("Expressive Files", state.appName)
        assertEquals("", state.version)
        assertTrue(state.sections.isEmpty())
    }

    @Test
    fun navigationUiState_preservesSectionDeclarationOrder() {
        val sections = listOf(
            DrawerSectionUi(id = "storages", title = "Storages"),
            DrawerSectionUi(id = "bookmarks", title = "Bookmarks"),
            DrawerSectionUi(
                id = "more",
                title = "More",
                entries = listOf(DrawerEntryUi(id = "action_settings", title = "Settings"))
            )
        )
        val state = NavigationUiState(version = "0.1.0", sections = sections)

        assertEquals(
            listOf("storages", "bookmarks", "more"),
            state.sections.map { it.id }
        )
        assertEquals(sections, state.sections)

        val versionBump = state.copy(version = "0.2.0")

        assertEquals("0.2.0", versionBump.version)
        assertEquals(state.appName, versionBump.appName)
        assertEquals(state.sections, versionBump.sections)
        assertNotSame(state, versionBump)
    }

    @Test
    fun breadcrumbUi_defaultsToANonCurrentCrumb() {
        val breadcrumb = BreadcrumbUi(
            path = "/storage/emulated/0",
            label = "Internal storage"
        )

        assertEquals("/storage/emulated/0", breadcrumb.path)
        assertEquals("Internal storage", breadcrumb.label)
        assertFalse(breadcrumb.isCurrent)
    }

    @Test
    fun breadcrumbTrail_marksExactlyOneCurrentCrumbAtTheEnd() {
        val trail = listOf(
            BreadcrumbUi(path = "/storage/emulated/0", label = "Internal storage"),
            BreadcrumbUi(path = "/storage/emulated/0/Download", label = "Download"),
            BreadcrumbUi(
                path = "/storage/emulated/0/Download/Invoices",
                label = "Invoices",
                isCurrent = true
            )
        )

        assertEquals(1, trail.count { it.isCurrent })
        assertEquals(trail.last(), trail.single { it.isCurrent })
        assertEquals(listOf(false, false, true), trail.map { it.isCurrent })
    }

    @Test
    fun fileListUiState_zeroSelectionKeepsBrowseChromeEligible() {
        val entries = listOf(
            FileEntryUi(path = "/storage/emulated/0/Download/a.pdf", name = "a.pdf"),
            FileEntryUi(
                path = "/storage/emulated/0/Download/Archive",
                name = "Archive",
                isDirectory = true
            )
        )
        val state = FileListUiState(
            title = "Download",
            subtitle = "Internal storage",
            entries = entries,
            selectedCount = 0
        )

        assertFalse(state.selectedCount > 0)
        assertTrue(state.entries.none { it.isSelected })

        // Mirrors the shell predicates: selection wins over the bottom navigation.
        val selectionMode = state.selectedCount > 0
        val pillEligible = !selectionMode && !state.isSearching

        assertFalse(selectionMode)
        assertTrue(pillEligible)
        assertEquals(2, state.entries.size)
    }
}
