/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.BreadcrumbUi
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.FileListUiState

/**
 * The stateless Expressive Files file browser.
 *
 * All folder contents, selection and chrome come from [state]. The screen deliberately hosts no
 * ViewModel and no sort sheet state: the caller owns both and receives all interactions as
 * callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    state: FileListUiState,
    onNavigateUp: () -> Unit,
    onBreadcrumbClick: (BreadcrumbUi) -> Unit,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    onSortClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAll: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateFile: () -> Unit,
    onPaste: () -> Unit,
    onSelectionShare: () -> Unit,
    onSelectionDelete: () -> Unit,
    onSelectionClear: () -> Unit,
    onSelectionMore: () -> Unit,
    bottomOverlayPadding: Dp = 0.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    var showListOptions by remember { mutableStateOf(false) }
    val selectionMode = state.selectedCount > 0
    val showFab = !selectionMode &&
        !state.isSearching &&
        !state.isLoading &&
        state.error == null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (selectionMode) {
                SelectionBar(
                    selectedCount = state.selectedCount,
                    onClose = onSelectionClear,
                    onShare = onSelectionShare,
                    onDelete = onSelectionDelete,
                    onMore = onSelectionMore
                )
            } else {
                FileListTopBar(
                    state = state,
                    onNavigateUp = onNavigateUp,
                    onSortClick = onSortClick,
                    onOptionsClick = { showListOptions = true }
                )
            }
            val ancestors = state.breadcrumbs.filterNot { it.isCurrent }
            if (ancestors.isNotEmpty()) {
                Breadcrumbs(
                    breadcrumbs = ancestors,
                    onBreadcrumbClick = onBreadcrumbClick
                )
            }
            if (state.isLoading && state.entries.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceContainerHighest
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.entries.isNotEmpty(),
                onRefresh = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                        )
                ) {
                    FileListBody(
                        state = state,
                        onEntryClick = onEntryClick,
                        onEntryLongClick = onEntryLongClick,
                        onEntryMenu = onEntryMenu,
                        onRefresh = onRefresh,
                        onCreateFolder = onCreateFolder
                    )
                }
            }
        }
        if (showFab) {
            CreateFabMenu(
                canPaste = state.canPaste,
                onCreateFolder = onCreateFolder,
                onCreateFile = onCreateFile,
                onPaste = onPaste,
                modifier = Modifier.padding(bottom = bottomOverlayPadding)
            )
        }
    }

    if (showListOptions) {
        ListOptionsSheet(
            isGrid = state.layoutMode == FileLayoutMode.GRID,
            onDismiss = { showListOptions = false },
            onSortClick = onSortClick,
            onRefresh = onRefresh,
            onSelectAll = onSelectAll,
            onToggleLayout = onLayoutToggle
        )
    }
}

@Composable
private fun FileListBody(
    state: FileListUiState,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit
) {
    val error = state.error
    val currentPath = state.breadcrumbs.lastOrNull { it.isCurrent }?.path
        ?: state.breadcrumbs.lastOrNull()?.path
        ?: state.title
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(currentPath, state.searchQuery) {
        listState.scrollToItem(0)
        gridState.scrollToItem(0)
    }

    when {
        error != null -> ErrorState(
            message = error,
            onRetry = onRefresh
        )
        state.isLoading && state.entries.isEmpty() -> LoadingState()
        state.entries.isEmpty() -> EmptyState(
            state = state,
            onCreateFolder = onCreateFolder
        )
        state.layoutMode == FileLayoutMode.GRID -> FileGrid(
            entries = state.entries,
            onEntryClick = onEntryClick,
            onEntryLongClick = onEntryLongClick,
            onEntryMenu = onEntryMenu,
            state = gridState
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = 4.dp, bottom = 112.dp)
        ) {
            items(
                items = state.entries,
                key = { entry -> entry.path }
            ) { entry ->
                FileRow(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onLongClick = { onEntryLongClick(entry) },
                    onMenuClick = { onEntryMenu(entry) }
                )
            }
        }
    }
}

@Preview(name = "File list - list", showBackground = true)
@Composable
private fun FileListScreenListPreview() {
    PreviewFileListScreen(state = previewState(FileLayoutMode.LIST))
}

@Preview(name = "File list - grid", showBackground = true)
@Composable
private fun FileListScreenGridPreview() {
    PreviewFileListScreen(state = previewState(FileLayoutMode.GRID))
}

@Preview(name = "File list - selection", showBackground = true)
@Composable
private fun FileListScreenSelectionPreview() {
    val state = previewState(FileLayoutMode.LIST)
    PreviewFileListScreen(
        state = state.copy(
            entries = state.entries.mapIndexed { index, entry ->
                entry.copy(isSelected = index == 0 || index == 2)
            },
            selectedCount = 2
        )
    )
}

@Composable
private fun PreviewFileListScreen(state: FileListUiState) {
    MaterialTheme {
        FileListScreen(
            state = state,
            onNavigateUp = {},
            onBreadcrumbClick = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEntryMenu = {},
            onSortClick = {},
            onLayoutToggle = {},
            onRefresh = {},
            onSelectAll = {},
            onCreateFolder = {},
            onCreateFile = {},
            onPaste = {},
            onSelectionShare = {},
            onSelectionDelete = {},
            onSelectionClear = {},
            onSelectionMore = {}
        )
    }
}

private fun previewState(layoutMode: FileLayoutMode): FileListUiState = FileListUiState(
    title = "Documents",
    subtitle = "Internal storage",
    breadcrumbs = listOf(
        BreadcrumbUi(
            path = "/storage/emulated/0",
            label = "Internal storage"
        ),
        BreadcrumbUi(
            path = "/storage/emulated/0/Documents",
            label = "Documents",
            isCurrent = true
        )
    ),
    entries = listOf(
        FileEntryUi(
            path = "/storage/emulated/0/Documents/Projects",
            name = "Projects",
            supportingText = "Folder",
            isDirectory = true,
            iconRes = R.drawable.file_directory_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Documents/Invoices",
            name = "Invoices",
            supportingText = "Folder",
            isDirectory = true,
            iconRes = R.drawable.file_directory_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Documents/roadmap.pdf",
            name = "roadmap.pdf",
            supportingText = "1.4 MB - Modified today",
            badgeText = "PDF",
            iconRes = R.drawable.file_pdf_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Documents/screenshot.png",
            name = "screenshot.png",
            supportingText = "860 kB - Modified yesterday",
            badgeText = "PNG",
            iconRes = R.drawable.file_image_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Documents/handover.zip",
            name = "handover.zip",
            supportingText = "24.8 MB - Modified 3 days ago",
            badgeText = "ZIP",
            iconRes = R.drawable.file_archive_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Documents/notes.txt",
            name = "notes.txt",
            supportingText = "2.1 kB - Modified last week",
            badgeText = "TXT",
            iconRes = R.drawable.file_generic_icon
        )
    ),
    layoutMode = layoutMode,
    sortLabel = "Name (A to Z)"
)
