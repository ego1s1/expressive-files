/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.search

import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.model.FileListUiState

@Preview(name = "Search - empty", showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    ExpressiveFilesPreview {
        SearchScreen(
            query = "budget",
            state = FileListUiState(isSearching = true),
            onQueryChange = {},
            onBack = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEntryMenu = {}
        )
    }
}

@Preview(name = "Search - results", showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    ExpressiveFilesPreview {
        SearchScreen(
            query = "report",
            state = previewSearchState(),
            onQueryChange = {},
            onBack = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEntryMenu = {}
        )
    }
}

@Preview(name = "Search - prompt", showBackground = true)
@Composable
private fun SearchScreenPromptPreview() {
    ExpressiveFilesPreview {
        SearchScreen(
            query = "",
            state = FileListUiState(isSearching = true),
            onQueryChange = {},
            onBack = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEntryMenu = {}
        )
    }
}

private fun previewSearchState(): FileListUiState = FileListUiState(
    title = "Search",
    isSearching = true,
    entries = listOf(
        FileEntryUi(
            path = "/storage/emulated/0/Documents/report-2026.pdf",
            name = "report-2026.pdf",
            supportingText = "Documents - 1.4 MB",
            badgeText = "PDF",
            iconRes = R.drawable.file_pdf_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Downloads/report-draft.docx",
            name = "report-draft.docx",
            supportingText = "Downloads - 640 kB",
            badgeText = "DOCX",
            iconRes = R.drawable.file_generic_icon
        ),
        FileEntryUi(
            path = "/storage/emulated/0/Pictures/reports",
            name = "reports",
            supportingText = "Pictures - Folder",
            isDirectory = true,
            iconRes = R.drawable.file_directory_icon
        )
    )
)
