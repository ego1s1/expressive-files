/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.HomeUiState
import me.zhanghai.android.files.compose.model.ShortcutEntryUi

/**
 * Recents or Favorites home.
 *
 * The active destination is inferred from [title] ("Favorites" shows [HomeUiState.favorites],
 * anything else shows [HomeUiState.recents]). The list is exposed as a single section so the host
 * can move to multi-section home data without restructuring the screen.
 */
@Composable
fun HomeScreen(
    title: String,
    state: HomeUiState,
    onOpen: (ShortcutEntryUi) -> Unit,
    onToggleFavorite: (ShortcutEntryUi) -> Unit,
    onOpenDrawer: (() -> Unit)?
) {
    val isFavorites = title.contains("favorite", ignoreCase = true)
    val entries = if (isFavorites) state.favorites else state.recents
    val supportingText = if (isFavorites) {
        "Files and folders you starred" // TODO: localize
    } else {
        "Recently opened files and folders" // TODO: localize
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize()) {
            HomeHeader(
                title = title,
                supportingText = supportingText,
                onOpenDrawer = onOpenDrawer
            )

            if (state.isLoading && entries.isNotEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            when {
                state.isLoading && entries.isEmpty() -> HomeLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                entries.isEmpty() -> HomeEmptyState(
                    isFavorites = isFavorites,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item(key = "home-section-header") {
                        HomeSectionHeader(isFavorites = isFavorites)
                    }
                    items(
                        items = entries,
                        key = { entry -> entry.path }
                    ) { entry ->
                        ShortcutRow(
                            entry = entry,
                            onOpen = { onOpen(entry) },
                            onToggleFavorite = { onToggleFavorite(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    title: String,
    supportingText: String,
    onOpenDrawer: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(start = if (onOpenDrawer != null) 4.dp else 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onOpenDrawer != null) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open navigation drawer" // TODO: localize
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeSectionHeader(isFavorites: Boolean) {
    Text(
        text = if (isFavorites) "Starred" else "Recently opened", // TODO: localize
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun HomeEmptyState(
    isFavorites: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isFavorites) {
                Icons.Outlined.Star
            } else {
                Icons.Filled.Refresh
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isFavorites) {
                "No favorites yet" // TODO: localize
            } else {
                "No recents yet" // TODO: localize
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isFavorites) {
                "Tap the star on any file or folder to keep it here." // TODO: localize
            } else {
                "Files and folders you open will appear here." // TODO: localize
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HomeLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Loading…", // TODO: localize
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val PreviewHomeState = HomeUiState(
    recents = listOf(
        ShortcutEntryUi(
            path = "/storage/emulated/0/DCIM/Camera/IMG_20260101_120000.jpg",
            name = "IMG_20260101_120000.jpg",
            subtitle = "Camera",
            isFavorite = true
        ),
        ShortcutEntryUi(
            path = "/storage/emulated/0/Download/Expressive Files.pdf",
            name = "Expressive Files.pdf",
            subtitle = "Download"
        ),
        ShortcutEntryUi(
            path = "/storage/emulated/0/Documents/Notes",
            name = "Notes",
            subtitle = "Documents",
            isDirectory = true
        )
    ),
    favorites = listOf(
        ShortcutEntryUi(
            path = "/storage/emulated/0/Pictures",
            name = "Pictures",
            subtitle = "Internal storage",
            isDirectory = true,
            isFavorite = true
        )
    )
)

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            title = "Recents",
            state = PreviewHomeState,
            onOpen = {},
            onToggleFavorite = {},
            onOpenDrawer = {}
        )
    }
}
