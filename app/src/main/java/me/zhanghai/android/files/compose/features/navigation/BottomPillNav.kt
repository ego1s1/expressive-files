/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons

/**
 * Floating icon-only navigation pill.
 *
 * Geometry and selection treatment follow the same floating-navigator pattern used by Iridium:
 * a circular surface, 48dp targets, a selected tonal pill, and no persistent labels. Labels are
 * exposed through tooltips and content descriptions for accessibility and pointer users.
 */
@Composable
fun BottomPillNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BottomNavDestinations.forEach { destination ->
                NavIcon(
                    selected = currentRoute == destination.route,
                    onClick = { onNavigate(destination.route) },
                    destination = destination
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavIcon(
    selected: Boolean,
    onClick: () -> Unit,
    destination: BottomNavDestination
) {
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        },
        label = "navContainer"
    )
    val content = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(destination.label) } },
        state = rememberTooltipState()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick, role = Role.Tab)
                .semantics { this.selected = selected }
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(container, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (destination.iconRes != 0) {
                    Icon(
                        painter = painterResource(destination.iconRes),
                        contentDescription = destination.label,
                        tint = content,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = destination.icon ?: ExpressiveIcons.Folder,
                        contentDescription = destination.label,
                        tint = content,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private val BottomNavDestinations = listOf(
    BottomNavDestination(
        route = "files",
        label = "Files", // TODO: localize
        icon = ExpressiveIcons.Folder
    ),
    BottomNavDestination(
        route = "recents",
        label = "Recents", // TODO: localize
        icon = Icons.Filled.Refresh
    ),
    BottomNavDestination(
        route = "favorites",
        label = "Favorites", // TODO: localize
        icon = Icons.Filled.Star
    ),
    BottomNavDestination(
        route = "locations",
        label = "Locations", // TODO: localize
        iconRes = R.drawable.device_icon_white_24dp
    )
)

private class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val iconRes: Int = 0
)
