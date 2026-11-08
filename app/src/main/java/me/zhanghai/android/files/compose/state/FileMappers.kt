/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.BreadcrumbUi
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatShort
import me.zhanghai.android.files.file.icon
import me.zhanghai.android.files.filelist.BreadcrumbData
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.util.Stateful

/**
 * Maps a [FileItem] from the legacy file list model into the immutable row model used by Compose.
 *
 * Symlink state is taken from the no-follow attributes so a link to a file is still rendered as a
 * link, while the icon and size use the followed attributes just like the legacy adapter.
 */
fun FileItem.toFileEntryUi(context: Context, selected: Boolean): FileEntryUi {
    val isDirectory = attributes.isDirectory
    val supportingText = if (isDirectory) {
        null
    } else {
        val lastModified = attributes.lastModifiedTime().toInstant().formatShort(context)
        val size = attributes.fileSize.formatHumanReadable(context)
        val separator = context.getString(R.string.file_item_description_separator)
        listOf(lastModified, size).joinToString(separator)
    }
    return FileEntryUi(
        path = path.toString(),
        name = name,
        supportingText = supportingText,
        isDirectory = isDirectory,
        isSelected = selected,
        isSymlink = attributesNoFollowLinks.isSymbolicLink,
        isHidden = isHidden,
        isArchive = path.isArchivePath,
        iconRes = mimeType.icon.resourceId,
        thumbnailKey = null,
        badgeText = null
    )
}

/**
 * Maps the legacy breadcrumb data into Compose breadcrumb chips.
 *
 * Only the trail up to and including the current folder is emitted: entries below the
 * selection are forward history from [TrailData.navigateUp] and must not be presented as
 * the current path.
 *
 * [currentPath] is used as a fallback when [BreadcrumbData.selectedIndex] is out of range (for
 * example while a new trail is being resolved).
 */
fun BreadcrumbData.toBreadcrumbUi(context: Context, currentPath: Path): List<BreadcrumbUi> {
    if (paths.isEmpty()) {
        return emptyList()
    }
    val currentIndex = selectedIndex.takeIf { it in paths.indices }
        ?: paths.indexOfFirst { it == currentPath }.takeIf { it >= 0 }
    return paths.mapIndexed { index, path ->
        val label = nameProducers.getOrNull(index)?.invoke(context) ?: path.name
        BreadcrumbUi(
            path = path.toString(),
            label = label,
            isCurrent = if (currentIndex != null) {
                index == currentIndex
            } else {
                path == currentPath
            }
        )
    }.filterIndexed { index, _ -> currentIndex == null || index <= currentIndex }
}

/**
 * Returns the last known entries for a stateful file list. This keeps the previous rows visible
 * while a reload is in flight or when a reload fails.
 */
fun Stateful<List<FileItem>>.entriesOrEmpty(previous: List<FileItem>): List<FileItem> =
    value ?: previous
