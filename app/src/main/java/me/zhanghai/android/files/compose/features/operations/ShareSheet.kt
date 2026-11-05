/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.operations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.icons.ExpressiveIcons

/**
 * Sheet with the share targets for a single file.
 *
 * Every action row is 56dp tall so the sheet remains comfortable to use one-handed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    fileName: String,
    supportingText: String?,
    onSend: () -> Unit,
    onSave: () -> Unit,
    onCopyPath: () -> Unit,
    onCompress: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        ShareSheetContent(
            fileName = fileName,
            supportingText = supportingText,
            onSend = onSend,
            onSave = onSave,
            onCopyPath = onCopyPath,
            onCompress = onCompress
        )
    }
}

@Composable
private fun ShareSheetContent(
    fileName: String,
    supportingText: String?,
    onSend: () -> Unit,
    onSave: () -> Unit,
    onCopyPath: () -> Unit,
    onCompress: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.share),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        ShareFileHeader(
            fileName = fileName,
            supportingText = supportingText,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(16.dp))
        ShareActionRow(
            icon = Icons.Filled.Share,
            label = stringResource(R.string.compose_send_to_app),
            onClick = onSend
        )
        ShareActionRow(
            icon = ExpressiveIcons.Folder,
            label = stringResource(R.string.compose_save_to_location),
            onClick = onSave
        )
        ShareActionRow(
            icon = Icons.Filled.List,
            label = stringResource(R.string.file_list_action_copy_path),
            onClick = onCopyPath
        )
        ShareActionRow(
            icon = Icons.Filled.Add,
            label = stringResource(R.string.compose_compress_and_share),
            onClick = onCompress
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ShareFileHeader(
    fileName: String,
    supportingText: String?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.file_generic_icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText != null && supportingText.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = colorScheme.surfaceContainer,
            contentColor = colorScheme.onSurfaceVariant
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurface
        )
    }
}
