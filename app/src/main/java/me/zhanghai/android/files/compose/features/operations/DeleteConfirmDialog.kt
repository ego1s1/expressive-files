/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.operations

import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Confirmation dialog for deleting [count] items.
 *
 * The confirm action is tinted with the error color and the dialog lists at most three names so
 * that the consequence of the action stays visible without letting the dialog grow unbounded.
 */
@Composable
fun DeleteConfirmDialog(
    count: Int,
    names: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        icon = { DeleteWarningIcon() },
        title = { Text(text = deleteTitle(count)) },
        text = { DeleteDialogBody(count = count, names = names) },
        confirmButton = { DeleteConfirmButton(onConfirm = onConfirm) },
        dismissButton = { DeleteDismissButton(onDismiss = onDismiss) }
    )
}

@Composable
private fun DeleteWarningIcon() {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = colorScheme.errorContainer,
        contentColor = colorScheme.onErrorContainer
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DeleteDialogBody(count: Int, names: List<String>) {
    val colorScheme = MaterialTheme.colorScheme
    val shownNames = names.take(3)
    val totalCount = maxOf(count, names.size)
    val moreCount = (totalCount - shownNames.size).coerceAtLeast(0)

    Column {
        Text(
            text = "This can't be undone.", // TODO: localize
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        if (shownNames.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    shownNames.forEach { entryName ->
                        Text(
                            text = entryName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (moreCount > 0) {
                        Text(
                            text = "+$moreCount more", // TODO: localize
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmButton(onConfirm: () -> Unit) {
    TextButton(
        onClick = onConfirm,
        modifier = Modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(text = "Delete") // TODO: localize
    }
}

@Composable
private fun DeleteDismissButton(onDismiss: () -> Unit) {
    TextButton(
        onClick = onDismiss,
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        Text(text = "Cancel") // TODO: localize
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun DeleteConfirmDialogPreview() {
    ExpressiveFilesPreview {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DeleteWarningIcon()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = deleteTitle(4),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    DeleteDialogBody(
                        count = 4,
                        names = listOf(
                            "IMG_2041.jpg",
                            "IMG_2042.jpg",
                            "IMG_2043.jpg",
                            "IMG_2044.jpg"
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeleteDismissButton(onDismiss = {})
                    Spacer(Modifier.size(8.dp))
                    DeleteConfirmButton(onConfirm = {})
                }
            }
        }
    }
}

private fun deleteTitle(count: Int): String = if (count == 1) {
    "Delete 1 item?" // TODO: localize
} else {
    "Delete $count items?" // TODO: localize
}
