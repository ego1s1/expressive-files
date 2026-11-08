/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons
import me.zhanghai.android.files.compose.model.FileListUiState

@Composable
internal fun EmptyState(
    state: FileListUiState,
    onCreateFolder: () -> Unit
) {
    CenteredMessageState(
        icon = ExpressiveIcons.Folder,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        headline = state.emptyTitle ?: stringResource(R.string.compose_this_folder_is_empty),
        body = state.emptyBody,
        actionLabel = stringResource(R.string.file_create_directory_title),
        actionIcon = Icons.Filled.Add,
        onAction = onCreateFolder
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
        headline = stringResource(R.string.compose_can_t_load_this_folder),
        body = message,
        actionLabel = stringResource(R.string.retry),
        actionIcon = Icons.Filled.Refresh,
        onAction = onRetry
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.compose_loading),
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
            color = MaterialTheme.colorScheme.onSurface,
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
