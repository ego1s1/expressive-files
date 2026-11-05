/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.operations

import me.zhanghai.android.files.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Dialog for renaming a file or folder.
 *
 * The base name (everything before the last extension dot) is preselected so typing replaces it
 * while keeping the extension intact.
 */
@Composable
fun RenameDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    RenameDialog(
        initialName = initialName,
        existingNames = emptyList(),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Overload that enables inline duplicate validation against [existingNames].
 */
@Composable
fun RenameDialog(
    initialName: String,
    existingNames: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var value by rememberSaveable(initialName, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialName,
                selection = TextRange(0, baseNameEnd(initialName))
            )
        )
    }
    var touched by rememberSaveable(initialName) { mutableStateOf(false) }
    val trimmedName = value.text.trim()
    val error = entryNameError(
        name = trimmedName,
        existingNames = existingNames.filterNot { it == initialName }
    )
    val showError = touched && error != null

    // The dialog window attaches after the first frame, so wait before claiming focus.
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(text = stringResource(R.string.rename)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        touched = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(text = stringResource(R.string.file_list_action_sort_by_name)) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.rename))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(text = stringResource(R.string.compose_cancel))
            }
        }
    )
}

private fun baseNameEnd(name: String): Int {
    val lastDot = name.lastIndexOf('.')
    return if (lastDot > 0) lastDot else name.length
}
