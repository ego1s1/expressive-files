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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private data class ArchiveFormatOption(val label: String, val value: String)

private val ArchiveFormatOptions = listOf(
    ArchiveFormatOption(label = "ZIP", value = "zip"), // TODO: localize
    ArchiveFormatOption(label = "TAR.XZ", value = "tar.xz"), // TODO: localize
    ArchiveFormatOption(label = "7Z", value = "7z") // TODO: localize
)

/**
 * Sheet for creating an archive from the selected items.
 *
 * @param onConfirm Receives the trimmed archive name, the format value (`"zip"`, `"tar.xz"` or
 * `"7z"`) and an optional password. The password is only non-null for ZIP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveSheet(
    defaultName: String,
    onConfirm: (name: String, format: String, password: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable(defaultName) { mutableStateOf(defaultName) }
    var formatIndex by rememberSaveable { mutableIntStateOf(0) }
    var password by rememberSaveable { mutableStateOf("") }
    var touched by rememberSaveable { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val selectedFormat = ArchiveFormatOptions[formatIndex]
    val isZip = selectedFormat.value == "zip"
    val trimmedName = name.trim()
    val nameError = entryNameError(trimmedName)
    val showNameError = touched && nameError != null

    fun submit() {
        touched = true
        if (nameError == null) {
            onConfirm(
                trimmedName,
                selectedFormat.value,
                if (isZip) password.takeIf { it.isNotEmpty() } else null
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
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
                Column {
                    Text(
                        text = "Create archive", // TODO: localize
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Choose a name and format", // TODO: localize
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
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Archive name") }, // TODO: localize
                singleLine = true,
                isError = showNameError,
                supportingText = if (showNameError) {
                    { Text(text = nameError.orEmpty()) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() })
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Format", // TODO: localize
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ArchiveFormatOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = index == formatIndex,
                        onClick = { formatIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ArchiveFormatOptions.size
                        ),
                        label = { Text(text = option.label) }
                    )
                }
            }
            if (isZip) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Password (optional)") }, // TODO: localize
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Leave empty to create an archive without a password." // TODO: localize
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            }
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
                    Text(text = "Cancel") // TODO: localize
                }
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(text = "Create") // TODO: localize
                }
            }
        }
    }
}
