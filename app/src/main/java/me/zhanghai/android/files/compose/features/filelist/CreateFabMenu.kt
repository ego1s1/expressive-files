/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons
import me.zhanghai.android.files.compose.theme.ExpressiveMotion
import me.zhanghai.android.files.compose.theme.rememberMotionEnabled

/**
 * Compact create menu items with staggered entrances. Positioning is owned by the caller;
 * pair with [CreateFabButton]. The tap-outside scrim is also the caller's job so the
 * button can sit in a centered group while the items stay on screen.
 */
@Composable
fun CreateFabMenuItems(
    canPaste: Boolean,
    onCreateFolder: () -> Unit,
    onCreateFile: () -> Unit,
    onPaste: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionEnabled = rememberMotionEnabled()

    BackHandler(enabled = expanded) {
        onExpandedChange(false)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
            AnimatedVisibility(
                visible = expanded,
                // The entrance is carried by the staggered items below, so the container
                // appears instantly; dismissal stays a single fast fade+slide for a
                // responsive feel.
                enter = EnterTransition.None,
                exit = if (motionEnabled) {
                    fadeOut(animationSpec = tween(120)) +
                        slideOutVertically(animationSpec = tween(150)) { height -> height / 3 }
                } else {
                    ExitTransition.None
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FabMenuItem(
                        visible = expanded,
                        // Items nearest the FAB lead the cascade.
                        enterDelayMillis = 80,
                        label = stringResource(R.string.file_create_directory_title),
                        onClick = {
                            onExpandedChange(false)
                            onCreateFolder()
                        }
                    ) {
                        Icon(
                            imageVector = ExpressiveIcons.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    FabMenuItem(
                        visible = expanded,
                        enterDelayMillis = 40,
                        label = stringResource(R.string.file_create_file_title),
                        onClick = {
                            onExpandedChange(false)
                            onCreateFile()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    FabMenuItem(
                        visible = expanded,
                        enterDelayMillis = 0,
                        label = stringResource(R.string.paste),
                        enabled = canPaste,
                        onClick = {
                            onExpandedChange(false)
                            onPaste()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.paste_icon_control_normal_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
    }
}

/**
 * M3 Expressive create button: a 56dp tonal circle riding beside the navigation pill,
 * matching its elevation and geometry instead of the default FAB treatment.
 */
@Composable
fun CreateFabButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionEnabled = rememberMotionEnabled()
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = if (motionEnabled) ExpressiveMotion.spatial() else snap(),
        label = "createFabIconRotation"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (expanded) {
                    stringResource(R.string.compose_close_create_menu)
                } else {
                    stringResource(R.string.compose_create_new_item)
                },
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    visible: Boolean,
    enterDelayMillis: Int,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionEnabled = rememberMotionEnabled()
    val contentColor = if (enabled) {
        colorScheme.onSurface
    } else {
        colorScheme.onSurface.copy(alpha = 0.38f)
    }
    AnimatedVisibility(
        visible = visible,
        enter = if (motionEnabled) {
            fadeIn(animationSpec = tween(durationMillis = 150, delayMillis = enterDelayMillis)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 220, delayMillis = enterDelayMillis)
                ) { height -> height / 3 } +
                scaleIn(
                    animationSpec = tween(durationMillis = 220, delayMillis = enterDelayMillis),
                    initialScale = 0.9f
                )
        } else {
            EnterTransition.None
        },
        // Dismissal is owned by the outer container so a mid-entrance collapse stays snappy.
        exit = ExitTransition.None
    ) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surfaceContainerHigh,
        contentColor = contentColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
    }
}
