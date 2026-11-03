/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.FileEntryUi

private val GridCellMinSize = 160.dp
private val GridSpacing = 12.dp

/**
 * Adaptive grid-mode file list. The caller owns the entries and all interaction state.
 */
@Composable
fun FileGrid(
    entries: List<FileEntryUi>,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState()
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = GridCellMinSize),
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 96.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(GridSpacing),
        verticalArrangement = Arrangement.spacedBy(GridSpacing)
    ) {
        items(
            items = entries,
            key = { entry -> entry.path }
        ) { entry ->
            FileGridTile(
                entry = entry,
                onClick = { onEntryClick(entry) },
                onLongClick = { onEntryLongClick(entry) },
                onMenuClick = { onEntryMenu(entry) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridTile(
    entry: FileEntryUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (entry.isSelected) {
        colorScheme.secondaryContainer
    } else {
        colorScheme.surfaceContainerHigh
    }
    val contentColor = if (entry.isSelected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurface
    }
    val secondaryContentColor = if (entry.isSelected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = "Select item", // TODO: localize
                role = Role.Button
            )
            .semantics { selected = entry.isSelected }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FileEntryIcon(
                    entry = entry,
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                text = entry.name,
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options for ${entry.name}", // TODO: localize
                tint = secondaryContentColor
            )
        }
        if (entry.isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .size(16.dp),
                tint = secondaryContentColor
            )
        }
    }
}
