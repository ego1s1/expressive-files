/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons

/**
 * Floating navigation pill.
 *
 * Geometry and selection treatment follow the same floating-navigator pattern used by Iridium:
 * a circular surface, 48dp targets, and a selected tonal pill. Only the selected destination
 * expands to show its label; the others stay icon-only. Labels remain available through
 * content descriptions and tooltips.
 */
@Composable
fun BottomPillNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    showSelectedLabel: Boolean = true
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
                    destination = destination,
                    showLabel = showSelectedLabel
                )
            }
        }
    }
}

/** Vertical floating navigation for medium and expanded windows. */
@Composable
fun PillNavRail(
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
        Column(
            modifier = Modifier
                .padding(4.dp)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
    destination: BottomNavDestination,
    showLabel: Boolean = false
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
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
        label = "navIconScale"
    )
    val haptics = LocalHapticFeedback.current
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(destination.label) } },
        state = rememberTooltipState(),
        // Icon-only chrome must not pop a label just because the item received focus.
        focusable = false
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onClick()
                    },
                    role = Role.Tab
                )
                .semantics {
                    this.selected = selected
                    contentDescription = destination.label
                }
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
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                } else {
                    Icon(
                        imageVector = destination.icon ?: ExpressiveIcons.Folder,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
                AnimatedVisibility(
                    visible = selected && showLabel,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = destination.label,
                            modifier = Modifier.clearAndSetSemantics {},
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
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
        route = "favorites",
        label = "Favorites", // TODO: localize
        iconRes = R.drawable.ic_expressive_star_filled
    ),
    BottomNavDestination(
        route = "locations",
        label = "Locations", // TODO: localize
        iconRes = R.drawable.device_icon_white_24dp
    ),
    BottomNavDestination(
        route = "settings",
        label = "Settings", // TODO: localize
        iconRes = R.drawable.settings_icon_white_24dp
    )
)

private class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val iconRes: Int = 0
)
