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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R

/**
 * Bottom sheet for creating an empty file or folder in the current directory.
 */
@Composable
fun CreateEntrySheet(
    isDirectory: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    CreateEntrySheet(
        isDirectory = isDirectory,
        existingNames = emptyList(),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Overload that enables inline duplicate validation against [existingNames].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEntrySheet(
    isDirectory: Boolean,
    existingNames: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        CreateEntrySheetContent(
            isDirectory = isDirectory,
            existingNames = existingNames,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CreateEntrySheetContent(
    isDirectory: Boolean,
    existingNames: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var name by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val trimmedName = name.trim()
    val error = entryNameError(trimmedName, existingNames)
    val showError = touched && error != null

    val title = if (isDirectory) {
        stringResource(R.string.file_create_directory_title)
    } else {
        stringResource(R.string.file_create_file_title)
    }
    val subtitle = if (isDirectory) {
        stringResource(R.string.compose_create_a_folder_in_this_location)
    } else {
        stringResource(R.string.compose_create_an_empty_file_in_this_location)
    }
    val label = if (isDirectory) {
        stringResource(R.string.compose_folder_name)
    } else {
        stringResource(R.string.file_list_create_file_name_hint)
    }

    // The sheet animates in, so wait for the first frame before claiming focus.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        focusRequester.requestFocus()
    }

    fun submit() {
        touched = true
        if (error == null) {
            onConfirm(trimmedName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                touched = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text(text = label) },
            singleLine = true,
            isError = showError,
            supportingText = if (showError) {
                { Text(text = error.orEmpty()) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.compose_cancel))
            }
            Button(
                onClick = { submit() },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.compose_create))
            }
        }
    }
}

/**
 * Returns the first inline validation message for [name], or `null` when the name is usable.
 *
 * This is shared by the create, rename and archive sheets so their rules stay consistent.
 */
@Composable
internal fun entryNameError(name: String, existingNames: List<String> = emptyList()): String? = when {
    name.isEmpty() -> stringResource(R.string.compose_enter_a_name)
    '/' in name -> stringResource(R.string.compose_a_name_can_t_contain)
    name == "." || name == ".." -> stringResource(R.string.compose_that_name_is_reserved)
    existingNames.any { it == name } -> {
        stringResource(R.string.compose_an_item_with_this_name_already_exists)
    }
    else -> null
}
