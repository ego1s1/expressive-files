/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import me.zhanghai.android.files.compose.icons.ExpressiveIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
@Composable
fun FileListScreen(
    state: FileListUiState,
    onOpenDrawer: () -> Unit,
    onNavigateUp: () -> Unit,
    onBreadcrumbClick: (BreadcrumbUi) -> Unit,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSortClick: () -> Unit,
    onLayoutToggle: () -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateFile: () -> Unit,
    onPaste: () -> Unit,
    onSelectionShare: () -> Unit,
    onSelectionDelete: () -> Unit,
    onSelectionClear: () -> Unit,
    onSelectionMore: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
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
                    onOpenDrawer = onOpenDrawer,
                    onNavigateUp = onNavigateUp,
                    onSearchClick = onSearchClick,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchClose = onSearchClose,
                    onSortClick = onSortClick,
                    onRefresh = onRefresh,
                    onLayoutToggle = onLayoutToggle
                )
            }
            if (state.breadcrumbs.isNotEmpty()) {
                Breadcrumbs(
                    breadcrumbs = state.breadcrumbs,
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                FileListBody(
                    state = state,
                    onEntryClick = onEntryClick,
                    onEntryLongClick = onEntryLongClick,
                    onEntryMenu = onEntryMenu,
                    onRefresh = onRefresh,
                    onCreateFolder = onCreateFolder,
                    onSearchClose = onSearchClose
                )
            }
        }
        if (showFab) {
            CreateFabMenu(
                canPaste = state.canPaste,
                onCreateFolder = onCreateFolder,
                onCreateFile = onCreateFile,
                onPaste = onPaste
            )
        }
    }
}

@Composable
private fun FileListBody(
    state: FileListUiState,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onSearchClose: () -> Unit
) {
    when {
        state.error != null -> ErrorState(
            message = state.error,
            onRetry = onRefresh
        )
        state.isLoading && state.entries.isEmpty() -> LoadingState()
        state.entries.isEmpty() -> EmptyState(
            state = state,
            onCreateFolder = onCreateFolder,
            onSearchClose = onSearchClose
        )
        state.layoutMode == FileLayoutMode.GRID -> FileGrid(
            entries = state.entries,
            onEntryClick = onEntryClick,
            onEntryLongClick = onEntryLongClick,
            onEntryMenu = onEntryMenu
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
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

@Composable
private fun EmptyState(
    state: FileListUiState,
    onCreateFolder: () -> Unit,
    onSearchClose: () -> Unit
) {
    val isSearching = state.isSearching
    val headline = state.emptyTitle ?: if (isSearching) {
        "No matching files" // TODO: localize
    } else {
        "This folder is empty" // TODO: localize
    }
    val actionLabel = if (isSearching) {
        "Clear search" // TODO: localize
    } else {
        "New folder" // TODO: localize
    }
    val actionIcon = if (isSearching) Icons.Filled.Close else Icons.Filled.Add
    val action = if (isSearching) onSearchClose else onCreateFolder

    CenteredMessageState(
        icon = ExpressiveIcons.Folder,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        headline = headline,
        body = state.emptyBody,
        actionLabel = actionLabel,
        actionIcon = actionIcon,
        onAction = action
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    CenteredMessageState(
        icon = Icons.Filled.Warning,
        iconTint = MaterialTheme.colorScheme.error,
        headline = "Can't load this folder", // TODO: localize
        body = message,
        actionLabel = "Retry", // TODO: localize
        actionIcon = Icons.Filled.Refresh,
        onAction = onRetry
    )
}

@Composable
private fun CenteredMessageState(
    icon: ImageVector,
    iconTint: Color,
    headline: String,
    body: String?,
    actionLabel: String,
    actionIcon: ImageVector,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = iconTint
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = headline,
            modifier = Modifier.widthIn(max = 360.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (!body.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 360.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(onClick = onAction) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(text = actionLabel)
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Loading...", // TODO: localize
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
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
            onOpenDrawer = {},
            onNavigateUp = {},
            onBreadcrumbClick = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEntryMenu = {},
            onSearchClick = {},
            onSearchQueryChange = {},
            onSearchClose = {},
            onSortClick = {},
            onLayoutToggle = {},
            onRefresh = {},
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
