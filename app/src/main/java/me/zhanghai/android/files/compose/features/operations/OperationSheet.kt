/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.operations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.OperationUi
import kotlin.math.roundToInt

/**
 * Modal sheet that reports a running file operation.
 *
 * A determinate progress bar is shown when [OperationUi.progress] is available; otherwise the bar
 * is indeterminate. While running the only action is cancel, and once complete the sheet offers a
 * dismissal action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationSheet(
    operation: OperationUi,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        OperationSheetContent(
            operation = operation,
            onCancel = onCancel,
            onDismiss = onDismiss,
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
internal fun OperationSheetContent(
    operation: OperationUi,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = operation.progress?.coerceIn(0f, 1f)
    val determinateProgress = when {
        operation.error != null -> null
        operation.isComplete -> 1f
        else -> progress
    }
    val detail = operation.detail
    val error = operation.error

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OperationStatusIcon(operation = operation)
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operation.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!detail.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        if (error != null) {
            OperationErrorCard(error = error)
        } else {
            val progressLabel = when {
                operation.isComplete -> "Complete" // TODO: localize
                progress != null -> "${(progress * 100).roundToInt()}%" // TODO: localize
                else -> null
            }
            if (progressLabel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = progressLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            if (determinateProgress != null) {
                LinearProgressIndicator(
                    progress = { determinateProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceContainer,
                    strokeCap = StrokeCap.Round
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceContainer,
                    strokeCap = StrokeCap.Round
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        when {
            operation.isComplete -> Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(text = "Done") // TODO: localize
            }
            operation.canCancel -> OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(text = "Cancel") // TODO: localize
            }
        }
    }
}

@Composable
private fun OperationStatusIcon(operation: OperationUi) {
    val colorScheme = MaterialTheme.colorScheme
    val icon = when {
        operation.error != null -> Icons.Filled.Warning
        operation.isComplete -> Icons.Filled.Check
        else -> Icons.Filled.Refresh
    }
    val containerColor = when {
        operation.error != null -> colorScheme.errorContainer
        operation.isComplete -> colorScheme.primaryContainer
        else -> colorScheme.secondaryContainer
    }
    val contentColor = when {
        operation.error != null -> colorScheme.onErrorContainer
        operation.isComplete -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun OperationErrorCard(error: String) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.errorContainer,
        contentColor = colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun OperationSheetPreview() {
    MaterialTheme {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            OperationSheetContent(
                operation = OperationUi(
                    id = "preview",
                    title = "Copying 3 items", // TODO: localize
                    detail = "/storage/emulated/0/DCIM/IMG_2041.jpg",
                    progress = 0.42f
                ),
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun OperationSheetErrorPreview() {
    MaterialTheme {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            OperationSheetContent(
                operation = OperationUi(
                    id = "preview-error",
                    title = "Copying 3 items", // TODO: localize
                    detail = "/storage/emulated/0/DCIM/IMG_2041.jpg",
                    isComplete = true,
                    error = "Not enough free space" // TODO: localize
                ),
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}
