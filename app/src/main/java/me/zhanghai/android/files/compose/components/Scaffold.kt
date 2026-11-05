/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.components

import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * App shell used by every Compose screen: a 64dp surface-container top bar with an optional
 * subtitle, plus Scaffold-managed snackbars, FAB, and system insets.
 */
@Composable
fun ExpressiveScaffold(
    title: String,
    subtitle: String? = null,
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            ExpressiveTopBar(
                title = title,
                subtitle = subtitle,
                onNavigateUp = onNavigateUp,
                actions = actions
            )
        },
        bottomBar = {
            // The bottom bar is owned by the caller; only the system inset is applied here.
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                bottomBar()
            }
        },
        snackbarHost = { ExpressiveSnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content
    )
}

@Composable
private fun ExpressiveTopBar(
    title: String,
    subtitle: String?,
    onNavigateUp: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateUp != null) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Navigate up" // TODO: localize
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (onNavigateUp != null) 4.dp else 0.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/** Contextual top bar shown while one or more items are selected. */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Exit selection" // TODO: localize
                )
            }
            Text(
                text = "$selectedCount selected", // TODO: localize
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpressiveScaffoldPreview() {
    ExpressiveFilesPreview {
        ExpressiveScaffold(
            title = "Files", // TODO: localize
            subtitle = "Internal storage", // TODO: localize
            onNavigateUp = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search" // TODO: localize
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Content", // TODO: localize
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
