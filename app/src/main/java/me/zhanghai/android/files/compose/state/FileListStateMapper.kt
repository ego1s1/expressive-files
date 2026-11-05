/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.FileListUiState
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.filelist.BreadcrumbData
import me.zhanghai.android.files.filelist.FileItemSet
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.getQuantityString

/**
 * Maps the legacy file list LiveData payload to the stateless Compose model.
 *
 * Loading and failure states keep the last known entries (which the legacy stateful values carry)
 * so the list does not flash empty while a directory is being refreshed.
 */
fun mapFileListState(
    context: Context,
    currentPath: Path?,
    stateful: Stateful<List<FileItem>>,
    breadcrumb: BreadcrumbData?,
    selectedFiles: FileItemSet,
    layoutMode: FileLayoutMode,
    sortLabel: String,
    query: String,
    isSearching: Boolean,
    canPaste: Boolean,
    canNavigateUp: Boolean
): FileListUiState {
    val files = stateful.value.orEmpty()
    val isLoading = stateful is Loading
    val error = (stateful as? Failure)?.throwable?.let {
        it.localizedMessage ?: it.toString()
    }
    val entries = files.map { file ->
        file.toFileEntryUi(context, selected = file in selectedFiles)
    }
    val breadcrumbPath = currentPath
        ?: breadcrumb?.let { data -> data.paths.getOrNull(data.selectedIndex) }
    val breadcrumbs = if (breadcrumb != null && breadcrumbPath != null) {
        breadcrumb.toBreadcrumbUi(context, breadcrumbPath)
    } else {
        emptyList()
    }
    val emptyTitle = when {
        !isLoading && error != null && entries.isEmpty() -> context.getString(R.string.error)
        !isLoading && error == null && entries.isEmpty() && isSearching ->
            context.getString(R.string.compose_no_matching_files)
        !isLoading && error == null && entries.isEmpty() ->
            context.getString(R.string.file_list_empty)
        else -> null
    }
    return FileListUiState(
        title = resolveTitle(context, currentPath, breadcrumb),
        subtitle = buildSubtitle(context, files),
        breadcrumbs = breadcrumbs,
        entries = entries,
        isLoading = isLoading,
        error = error,
        emptyTitle = emptyTitle,
        emptyBody = null,
        selectedCount = selectedFiles.size,
        layoutMode = layoutMode,
        sortLabel = sortLabel,
        searchQuery = query,
        isSearching = isSearching,
        canPaste = canPaste,
        canNavigateUp = canNavigateUp
    )
}

private fun resolveTitle(
    context: Context,
    currentPath: Path?,
    breadcrumb: BreadcrumbData?
): String {
    val breadcrumbName = breadcrumb?.let { data ->
        data.nameProducers.getOrNull(data.selectedIndex)?.invoke(context)
            ?: data.paths.getOrNull(data.selectedIndex)?.name
    }
    return breadcrumbName ?: currentPath?.name ?: context.getString(R.string.file_list_title)
}

private fun buildSubtitle(context: Context, files: List<FileItem>): String? {
    if (files.isEmpty()) {
        return null
    }
    val directoryCount = files.count { it.attributes.isDirectory }
    val fileCount = files.size - directoryCount
    val parts = buildList {
        if (directoryCount > 0) {
            add(
                context.getQuantityString(
                    R.plurals.file_list_subtitle_directory_count_format,
                    directoryCount,
                    directoryCount
                )
            )
        }
        if (fileCount > 0) {
            add(
                context.getQuantityString(
                    R.plurals.file_list_subtitle_file_count_format,
                    fileCount,
                    fileCount
                )
            )
        }
    }
    return if (parts.isEmpty()) {
        context.getString(R.string.empty)
    } else {
        parts.joinToString(context.getString(R.string.file_list_subtitle_separator))
    }
}
