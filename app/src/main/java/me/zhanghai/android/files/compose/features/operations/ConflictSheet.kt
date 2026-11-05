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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.ConflictUi

/**
 * Sheet shown when a file operation would overwrite an existing item.
 *
 * The rename field starts with a suggested "name (1).ext" value so the rename action is immediately
 * usable. The "Apply to all" switch is local UI state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictSheet(
    conflict: ConflictUi,
    onReplace: () -> Unit,
    onSkip: () -> Unit,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        ConflictSheetContent(
            conflict = conflict,
            onReplace = onReplace,
            onSkip = onSkip,
            onRename = onRename,
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
private fun ConflictSheetContent(
    conflict: ConflictUi,
    onReplace: () -> Unit,
    onSkip: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var applyToAll by rememberSaveable(conflict.fileName, conflict.existingName) {
        mutableStateOf(false)
    }
    var renameName by rememberSaveable(conflict.fileName, conflict.existingName) {
        mutableStateOf(suggestedCopyName(conflict.fileName))
    }
    var touched by rememberSaveable(conflict.fileName, conflict.existingName) {
        mutableStateOf(false)
    }
    val trimmedName = renameName.trim()
    val renameError = entryNameError(trimmedName, listOf(conflict.existingName))
    val showRenameError = touched && renameError != null

    fun submitRename() {
        touched = true
        if (renameError == null) {
            onRename(trimmedName)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.compose_file_already_exists),
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        ConflictFileCard(conflict = conflict)
        if (conflict.applyToAllAllowed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(
                        value = applyToAll,
                        role = Role.Switch,
                        onValueChange = { applyToAll = it }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.compose_apply_to_all),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.compose_use_this_choice_for_the_remaining_conflicts),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.size(12.dp))
                Switch(checked = applyToAll, onCheckedChange = null)
            }
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = renameName,
            onValueChange = {
                renameName = it
                touched = true
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.file_job_conflict_name_hint)) },
            singleLine = true,
            isError = showRenameError,
            supportingText = if (showRenameError) {
                { Text(text = renameError.orEmpty()) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitRename() })
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.skip))
            }
            FilledTonalButton(
                onClick = { submitRename() },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                enabled = renameError == null
            ) {
                Text(text = stringResource(R.string.rename))
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onReplace,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.replace))
        }
    }
}

@Composable
private fun ConflictFileCard(conflict: ConflictUi) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conflict.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.compose_already_exists,
                        conflict.existingName
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Suggests "name (1).ext" for [name], keeping the extension intact. */
private fun suggestedCopyName(name: String): String {
    val lastDot = name.lastIndexOf('.')
    return if (lastDot > 0) {
        "${name.substring(0, lastDot)} (1)${name.substring(lastDot)}"
    } else {
        "$name (1)"
    }
}
