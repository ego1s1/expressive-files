/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.icons.ExpressiveIcons
import me.zhanghai.android.files.compose.model.DrawerEntryUi
import me.zhanghai.android.files.compose.model.NavigationUiState

/**
 * Collapsed (80dp) navigation rail for compact-width-free layouts.
 *
 * Only the top-level destinations are repeated from the drawer; storage/bookmark entries stay in
 * [AppDrawer], so [onEntryClick] is intentionally unused. The rail header is left empty for the
 * host's FAB.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AppNavigationRail(
    state: NavigationUiState,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onEntryClick: (DrawerEntryUi) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        railDestinations.forEach { destination ->
            NavigationRailItem(
                selected = isRailRouteSelected(currentRoute, destination.route),
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = railLabel(state, destination),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.width(48.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(4.dp))

        NavigationRailItem(
            selected = false,
            onClick = onSettings,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings" // TODO: localize
                )
            }
        )
        NavigationRailItem(
            selected = false,
            onClick = onAbout,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "About" // TODO: localize
                )
            }
        )
    }
}

private data class RailDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val railDestinations = listOf(
    RailDestination("files", "Files", ExpressiveIcons.Folder), // TODO: localize
    RailDestination("recents", "Recents", Icons.Filled.Refresh), // TODO: localize
    RailDestination("favorites", "Favorites", Icons.Filled.Star) // TODO: localize
)

private fun railLabel(state: NavigationUiState, destination: RailDestination): String =
    state.sections
        .firstOrNull { section ->
            section.id == destination.route && !section.title.isNullOrBlank()
        }
        ?.title
        ?: destination.label

private fun isRailRouteSelected(currentRoute: String, route: String): Boolean =
    currentRoute == route || currentRoute.startsWith("$route/")
