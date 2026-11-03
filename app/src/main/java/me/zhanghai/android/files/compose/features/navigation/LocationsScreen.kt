/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.DrawerEntryUi
import me.zhanghai.android.files.compose.model.DrawerSectionUi
import me.zhanghai.android.files.compose.model.NavigationUiState

/**
 * Storage and bookmark hub. This replaces the drawer's shortcut sections now that global
 * navigation lives in [BottomPillNav].
 */
@Composable
fun LocationsScreen(
    state: NavigationUiState,
    onEntryClick: (DrawerEntryUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = state.sections.filter {
        it.id != "favorites" && it.id != "recents" && it.id != "more"
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize()) {
            LocationsHeader()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp)
            ) {
                sections.forEach { section ->
                    if (!section.title.isNullOrBlank()) {
                        item(key = "header/${section.id}") {
                            SectionLabel(section.title)
                        }
                    }
                    items(
                        items = section.entries,
                        key = { "${section.id}/${it.id}" }
                    ) { entry ->
                        LocationRow(entry = entry, onClick = { onEntryClick(entry) })
                    }
                    item(key = "gap/${section.id}") {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(112.dp)
            .padding(start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Locations", // TODO: localize
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Storages and bookmarks", // TODO: localize
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LocationRow(
    entry: DrawerEntryUi,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(entry.title) },
        supportingContent = entry.subtitle?.let { subtitle ->
            { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        },
        leadingContent = {
            if (entry.iconRes != 0) {
                Icon(
                    painter = painterResource(entry.iconRes),
                    contentDescription = null
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    )
}

