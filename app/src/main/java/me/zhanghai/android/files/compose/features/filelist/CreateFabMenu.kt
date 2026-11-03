/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons

/**
 * A compact, scrim-less create menu built from a [FloatingActionButton] and
 * [AnimatedVisibility], avoiding Material3 1.5-only FAB menu APIs.
 */
@Composable
fun CreateFabMenu(
    canPaste: Boolean,
    onCreateFolder: () -> Unit,
    onCreateFile: () -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "createFabIconRotation"
    )

    BackHandler(enabled = expanded) {
        expanded = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clearAndSetSemantics {}
                    .pointerInput(Unit) {
                        detectTapGestures { expanded = false }
                    }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(150)) +
                    slideInVertically(animationSpec = tween(200)) { height -> height / 3 },
                exit = fadeOut(animationSpec = tween(120)) +
                    slideOutVertically(animationSpec = tween(150)) { height -> height / 3 }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FabMenuItem(
                        label = "New folder", // TODO: localize
                        onClick = {
                            expanded = false
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
                        label = "New file", // TODO: localize
                        onClick = {
                            expanded = false
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
                        label = "Paste", // TODO: localize
                        enabled = canPaste,
                        onClick = {
                            expanded = false
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
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (expanded) {
                        "Close create menu" // TODO: localize
                    } else {
                        "Create new item" // TODO: localize
                    },
                    modifier = Modifier.rotate(iconRotation)
                )
            }
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = if (enabled) {
        colorScheme.onSurface
    } else {
        colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surfaceContainerHigh,
        contentColor = contentColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
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
