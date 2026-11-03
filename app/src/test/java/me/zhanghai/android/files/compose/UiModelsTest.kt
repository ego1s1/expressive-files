/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import me.zhanghai.android.files.compose.model.BreadcrumbUi
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.FileListUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for the stateless UI models shared by the Compose screens.
 *
 * These models are deliberately dumb: they do not clamp invalid input or compute selection
 * counts. The selection invariant (`selectedCount >= 0`) is therefore verified for the states
 * the UI is expected to construct, not by expecting the data class to reject bad values.
 */
class UiModelsTest {

    @Test
    fun fileEntryUi_appliesDocumentedDefaults() {
        val entry = FileEntryUi(
            path = "/storage/emulated/0/Download",
            name = "Download"
        )

        assertEquals("/storage/emulated/0/Download", entry.path)
        assertEquals("Download", entry.name)
        assertNull(entry.supportingText)
        assertFalse(entry.isDirectory)
        assertFalse(entry.isSelected)
        assertFalse(entry.isSymlink)
        assertFalse(entry.isHidden)
        assertFalse(entry.isArchive)
        assertEquals(0, entry.iconRes)
        assertNull(entry.thumbnailKey)
        assertNull(entry.badgeText)
    }

    @Test
    fun fileEntryUi_copyOnlyChangesRequestedFields() {
        val original = FileEntryUi(
            path = "/storage/emulated/0/notes.txt",
            name = "notes.txt",
            supportingText = "2.1 kB",
            isDirectory = false,
            isSelected = false,
            isSymlink = true,
            isHidden = false,
            isArchive = false,
            iconRes = 42,
            thumbnailKey = "notes-thumb",
            badgeText = "TXT"
        )

        val selected = original.copy(isSelected = true)

        assertTrue(selected.isSelected)
        assertEquals(original.path, selected.path)
        assertEquals(original.name, selected.name)
        assertEquals(original.supportingText, selected.supportingText)
        assertEquals(original.isDirectory, selected.isDirectory)
        assertEquals(original.isSymlink, selected.isSymlink)
        assertEquals(original.isHidden, selected.isHidden)
        assertEquals(original.isArchive, selected.isArchive)
        assertEquals(original.iconRes, selected.iconRes)
        assertEquals(original.thumbnailKey, selected.thumbnailKey)
        assertEquals(original.badgeText, selected.badgeText)
        assertFalse(original.isSelected)
        assertNotSame(original, selected)
    }

    @Test
    fun fileEntryUi_equalityAndHashCodeAreStructural() {
        val entry = FileEntryUi(path = "/a", name = "a", isDirectory = true)

        val same = entry.copy()
        val different = entry.copy(name = "b")

        assertEquals(entry, same)
        assertEquals(entry.hashCode(), same.hashCode())
        assertNotEquals(entry, different)
    }

    @Test
    fun fileListUiState_defaultsDescribeAnEmptyDirectory() {
        val state = FileListUiState()

        assertEquals("", state.title)
        assertNull(state.subtitle)
        assertTrue(state.breadcrumbs.isEmpty())
        assertTrue(state.entries.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.emptyTitle)
        assertNull(state.emptyBody)
        assertEquals(0, state.selectedCount)
        assertEquals(FileLayoutMode.LIST, state.layoutMode)
        assertEquals("", state.sortLabel)
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertFalse(state.canPaste)
        assertFalse(state.canNavigateUp)
    }

    @Test
    fun fileListUiState_selectedCountMatchesSelectedEntries() {
        val entries = listOf(
            FileEntryUi(path = "/a", name = "a", isSelected = true),
            FileEntryUi(path = "/b", name = "b", isSelected = false),
            FileEntryUi(path = "/c", name = "c", isSelected = true)
        )

        val state = FileListUiState(
            entries = entries,
            selectedCount = entries.count { it.isSelected }
        )

        assertTrue(state.selectedCount >= 0)
        assertEquals(2, state.selectedCount)
        assertEquals(state.selectedCount, state.entries.count { it.isSelected })
        assertEquals(3, state.entries.size)
    }

    @Test
    fun fileListUiState_copyPreservesSelectionInvariant() {
        val base = FileListUiState(
            title = "Download",
            breadcrumbs = listOf(
                BreadcrumbUi(
                    path = "/storage/emulated/0",
                    label = "Internal storage"
                )
            ),
            entries = listOf(
                FileEntryUi(path = "/storage/emulated/0/Download/a", name = "a"),
                FileEntryUi(path = "/storage/emulated/0/Download/b", name = "b")
            )
        )
        assertEquals(0, base.selectedCount)

        val oneSelected = base.copy(
            entries = base.entries.mapIndexed { index, entry ->
                entry.copy(isSelected = index == 0)
            },
            selectedCount = 1
        )
        assertTrue(oneSelected.selectedCount >= 0)
        assertEquals(1, oneSelected.selectedCount)
        assertEquals(1, oneSelected.entries.count { it.isSelected })

        val cleared = oneSelected.copy(
            entries = oneSelected.entries.map { it.copy(isSelected = false) },
            selectedCount = 0
        )
        assertEquals(0, cleared.selectedCount)
        assertTrue(cleared.entries.none { it.isSelected })
        assertEquals(base.title, cleared.title)
        assertEquals(base.breadcrumbs, cleared.breadcrumbs)
    }

    @Test
    fun fileListUiState_emptyResultCanStillCarryChrome() {
        val state = FileListUiState(
            title = "Movies",
            subtitle = "Internal storage",
            emptyTitle = "No movies here",
            emptyBody = "Copy a video into this folder to see it.",
            sortLabel = "Name (A to Z)"
        )

        assertTrue(state.entries.isEmpty())
        assertEquals(0, state.selectedCount)
        assertEquals("No movies here", state.emptyTitle)
        assertEquals("Copy a video into this folder to see it.", state.emptyBody)
        assertEquals("Internal storage", state.subtitle)
        assertEquals("Name (A to Z)", state.sortLabel)
    }

    @Test
    fun fileLayoutMode_declaresListThenGrid() {
        assertEquals(listOf("LIST", "GRID"), FileLayoutMode.entries.map { it.name })
        assertEquals(FileLayoutMode.LIST, FileLayoutMode.valueOf("LIST"))
        assertEquals(FileLayoutMode.GRID, FileLayoutMode.valueOf("GRID"))
        assertEquals(2, FileLayoutMode.entries.size)
    }

    @Test
    fun fileLayoutMode_roundTripsThroughStateCopy() {
        val listState = FileListUiState(title = "Pictures")
        assertEquals(FileLayoutMode.LIST, listState.layoutMode)

        val gridState = listState.copy(layoutMode = FileLayoutMode.GRID)
        assertEquals(FileLayoutMode.GRID, gridState.layoutMode)
        assertEquals("Pictures", gridState.title)
        assertEquals(FileLayoutMode.LIST, listState.layoutMode)
    }
}
