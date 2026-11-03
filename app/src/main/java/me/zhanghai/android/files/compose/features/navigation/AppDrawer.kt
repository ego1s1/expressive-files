/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons
import me.zhanghai.android.files.compose.model.DrawerEntryUi
import me.zhanghai.android.files.compose.model.DrawerSectionUi
import me.zhanghai.android.files.compose.model.NavigationUiState

/**
 * Modal navigation drawer content.
 *
 * Place this inside a [androidx.compose.material3.ModalNavigationDrawer] whose drawer state
 * handles predictive back. [onClose] is invoked by the explicit close affordance so the host can
 * animate the drawer shut.
 */
@Composable
fun AppDrawer(
    state: NavigationUiState,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onEntryClick: (DrawerEntryUi) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 360.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        DrawerHeader(
            appName = state.appName,
            version = state.version,
            onClose = onClose
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            drawerDestinations.forEach { destination ->
                item(key = "destination/${destination.route}") {
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = isRouteSelected(currentRoute, destination.route),
                        onClick = { onNavigate(destination.route) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Action entries (Settings/About) are rendered by the dedicated bottom rows below, so
            // keep the state-driven sections limited to navigable shortcuts.
            val sections = state.sections
                .map { section ->
                    section.copy(entries = section.entries.filterNot { it.isAction })
                }
                .filter { it.entries.isNotEmpty() }
            if (sections.isNotEmpty()) {
                item(key = "destination-divider") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                sections.forEach { section ->
                    if (!section.title.isNullOrBlank()) {
                        item(key = "header/${section.id}") {
                            DrawerSectionHeader(title = section.title)
                        }
                    }
                    items(
                        items = section.entries,
                        key = { entry -> "${section.id}/${entry.id}" }
                    ) { entry ->
                        DrawerEntryRow(
                            entry = entry,
                            isFavorite = section.isFavoritesSection,
                            onClick = { onEntryClick(entry) }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        NavigationDrawerItem(
            label = { Text("Settings") }, // TODO: localize
            selected = false,
            onClick = onSettings,
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("About") }, // TODO: localize
            selected = false,
            onClick = onAbout,
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DrawerHeader(
    appName: String,
    version: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.launcher_icon_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (version.isNotBlank()) {
                Text(
                    text = version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close navigation drawer" // TODO: localize
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DrawerEntryRow(
    entry: DrawerEntryUi,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val selected = entry.selected
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                }
            )
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawerEntryIcon(
            iconRes = entry.iconRes,
            tint = if (selected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!entry.subtitle.isNullOrBlank()) {
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = supportingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!entry.badge.isNullOrBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = entry.badge,
                style = MaterialTheme.typography.labelSmall,
                color = supportingColor,
                maxLines = 1
            )
        }
        if (isFavorite) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Favorite", // TODO: localize
                modifier = Modifier.size(18.dp),
                tint = if (selected) contentColor else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DrawerEntryIcon(
    @DrawableRes iconRes: Int,
    tint: Color
) {
    if (iconRes != 0) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
    } else {
        Icon(
            imageVector = ExpressiveIcons.Folder,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
    }
}

private data class DrawerDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val drawerDestinations = listOf(
    DrawerDestination("files", "Files", ExpressiveIcons.Folder), // TODO: localize
    DrawerDestination("recents", "Recents", Icons.Filled.Refresh), // TODO: localize
    DrawerDestination("favorites", "Favorites", Icons.Filled.Star) // TODO: localize
)

private val DrawerSectionUi.isFavoritesSection: Boolean
    get() = id.contains("favorite", ignoreCase = true) ||
        title?.contains("favorite", ignoreCase = true) == true

private fun isRouteSelected(currentRoute: String, route: String): Boolean =
    currentRoute == route || currentRoute.startsWith("$route/")

private val PreviewNavigationState = NavigationUiState(
    version = "1.7.5 (40)",
    sections = listOf(
        DrawerSectionUi(
            id = "favorites",
            title = "Favorites",
            entries = listOf(
                DrawerEntryUi(
                    id = "/storage/emulated/0/Pictures",
                    title = "Pictures",
                    subtitle = "/storage/emulated/0/Pictures"
                )
            )
        ),
        DrawerSectionUi(
            id = "storages",
            title = "Storages",
            entries = listOf(
                DrawerEntryUi(
                    id = "internal",
                    title = "Internal storage",
                    subtitle = "/storage/emulated/0"
                )
            )
        ),
        DrawerSectionUi(
            id = "bookmarks",
            title = "Bookmarks",
            entries = listOf(
                DrawerEntryUi(
                    id = "downloads",
                    title = "Downloads",
                    subtitle = "/storage/emulated/0/Download"
                )
            )
        )
    )
)

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AppDrawerPreview() {
    MaterialTheme {
        AppDrawer(
            state = PreviewNavigationState,
            currentRoute = "files",
            onNavigate = {},
            onEntryClick = {},
            onSettings = {},
            onAbout = {},
            onClose = {}
        )
    }
}
