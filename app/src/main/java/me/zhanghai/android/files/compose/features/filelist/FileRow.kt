/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileEntryUi

private const val LIST_ROW_HEIGHT_DP = 72
private const val LIST_ICON_SIZE_DP = 40

/**
 * A single list-mode row for [entry].
 *
 * The row is intentionally dumb: selection, opening and menus are all driven by the caller.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    entry: FileEntryUi,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (entry.isSelected) {
        colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val titleColor = if (entry.isSelected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurface
    }
    val supportingColor = if (entry.isSelected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }
    val trailingColor = if (entry.isSelected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }
    val supportingText = entry.supportingText
    val badgeText = entry.badgeText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LIST_ROW_HEIGHT_DP.dp)
            .background(containerColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = "Select item", // TODO: localize
                role = Role.Button
            )
            .semantics { selected = entry.isSelected }
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileEntryIcon(
            entry = entry,
            modifier = Modifier.size(LIST_ICON_SIZE_DP.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.name,
                    modifier = Modifier.weight(1f),
                    color = titleColor,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (badgeText != null) {
                    Spacer(Modifier.width(8.dp))
                    TypeBadge(text = badgeText, selected = entry.isSelected)
                }
            }
            if (!supportingText.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supportingText,
                    color = supportingColor,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (entry.isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options for ${entry.name}", // TODO: localize
                tint = trailingColor
            )
        }
    }
}

/**
 * Loads the model-provided icon, with a safe fallback for hosts that do not provide one.
 *
 * The icon is decorative: the entry name right next to it is the accessible label.
 */
@Composable
internal fun FileEntryIcon(
    entry: FileEntryUi,
    modifier: Modifier = Modifier
) {
    val iconRes = when {
        entry.iconRes != 0 -> entry.iconRes
        entry.isDirectory -> R.drawable.file_directory_icon
        else -> R.drawable.file_generic_icon
    }
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun TypeBadge(
    text: String,
    selected: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (selected) {
        colorScheme.onSecondaryContainer.copy(alpha = 0.12f)
    } else {
        colorScheme.surfaceContainerHighest
    }
    val contentColor = if (selected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 18.dp)
                .padding(horizontal = 6.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}
