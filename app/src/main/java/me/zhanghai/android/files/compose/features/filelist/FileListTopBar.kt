/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.FileListUiState

private const val TOP_BAR_HEIGHT_DP = 64

/**
 * The file list app bar: navigation, title/subtitle, search/sort/overflow actions, and the
 * inline search field.
 */
@Composable
fun FileListTopBar(
    state: FileListUiState,
    onOpenDrawer: (() -> Unit)?,
    onNavigateUp: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSortClick: () -> Unit,
    onRefresh: () -> Unit,
    onLayoutToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        if (state.isSearching) {
            SearchTopBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                onClose = onSearchClose
            )
        } else {
            BrowseTopBar(
                state = state,
                onOpenDrawer = onOpenDrawer,
                onNavigateUp = onNavigateUp,
                onSearchClick = onSearchClick,
                onSortClick = onSortClick,
                onRefresh = onRefresh,
                onLayoutToggle = onLayoutToggle
            )
        }
    }
}

@Composable
private fun BrowseTopBar(
    state: FileListUiState,
    onOpenDrawer: (() -> Unit)?,
    onNavigateUp: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onRefresh: () -> Unit,
    onLayoutToggle: () -> Unit
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val title = state.title.ifBlank { "Files" } // TODO: localize
    val subtitle = state.subtitle
    val sortDescription = if (state.sortLabel.isBlank()) {
        "Sort and view" // TODO: localize
    } else {
        "Sort and view, sorted by ${state.sortLabel}" // TODO: localize
    }
    val layoutToggleLabel = if (state.layoutMode == FileLayoutMode.LIST) {
        "Switch to grid" // TODO: localize
    } else {
        "Switch to list" // TODO: localize
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TOP_BAR_HEIGHT_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.canNavigateUp || onOpenDrawer != null) {
            IconButton(
                onClick = if (state.canNavigateUp) onNavigateUp else onOpenDrawer!!,
                modifier = Modifier.size(48.dp)
            ) {
                if (state.canNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate up" // TODO: localize
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.menu_icon_control_normal_24dp),
                        contentDescription = "Open navigation drawer", // TODO: localize
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search files" // TODO: localize
            )
        }
        IconButton(
            onClick = onSortClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.sort_icon_control_normal_24dp),
                contentDescription = sortDescription,
                modifier = Modifier.size(24.dp)
            )
        }
        Box {
            IconButton(
                onClick = { overflowExpanded = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options" // TODO: localize
                )
            }
            DropdownMenu(
                expanded = overflowExpanded,
                onDismissRequest = { overflowExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Refresh") }, // TODO: localize
                    onClick = {
                        overflowExpanded = false
                        onRefresh()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(layoutToggleLabel) },
                    onClick = {
                        overflowExpanded = false
                        onLayoutToggle()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TOP_BAR_HEIGHT_DP.dp)
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = CircleShape,
            color = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search files", // TODO: localize
                                    color = colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            focusRequester.requestFocus()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search", // TODO: localize
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
            }
        }
        IconButton(
            onClick = {
                keyboardController?.hide()
                onClose()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close search" // TODO: localize
            )
        }
    }
}
