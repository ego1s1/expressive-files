/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.icons.ExpressiveIcons
import me.zhanghai.android.files.compose.model.FileListUiState

@Composable
internal fun EmptyState(
    state: FileListUiState,
    onCreateFolder: () -> Unit,
    onSearchClose: () -> Unit
) {
    val isSearching = state.isSearching
    val headline = state.emptyTitle ?: if (isSearching) {
        "No matching files" // TODO: localize
    } else {
        "This folder is empty" // TODO: localize
    }
    val actionLabel = if (isSearching) {
        "Clear search" // TODO: localize
    } else {
        "New folder" // TODO: localize
    }
    val actionIcon = if (isSearching) Icons.Filled.Close else Icons.Filled.Add
    val action = if (isSearching) onSearchClose else onCreateFolder

    CenteredMessageState(
        icon = ExpressiveIcons.Folder,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        headline = headline,
        body = state.emptyBody,
        actionLabel = actionLabel,
        actionIcon = actionIcon,
        onAction = action
    )
}

@Composable
internal fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    CenteredMessageState(
        icon = Icons.Filled.Warning,
        iconTint = MaterialTheme.colorScheme.error,
        headline = "Can't load this folder", // TODO: localize
        body = message,
        actionLabel = "Retry", // TODO: localize
        actionIcon = Icons.Filled.Refresh,
        onAction = onRetry
    )
}

@Composable
internal fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Loading...", // TODO: localize
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CenteredMessageState(
    icon: ImageVector,
    iconTint: Color,
    headline: String,
    body: String?,
    actionLabel: String,
    actionIcon: ImageVector,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = iconTint
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = headline,
            modifier = Modifier.widthIn(max = 360.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (!body.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 360.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(onClick = onAction) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(text = actionLabel)
        }
    }
}
