/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileLayoutMode

/**
 * Sort keys exposed by the Compose file list.
 *
 * The shared model only carries a preformatted sort label, so this richer, typed option set lives
 * with the screen until the integrator wires real sort state.
 */
enum class FileSortBy {
    NAME,
    TYPE,
    SIZE,
    LAST_MODIFIED
}

/**
 * Bottom sheet for choosing the layout mode, sort key, sort direction and folder policy.
 *
 * The sheet is stateless: callers update their sort state in response to the callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    layoutMode: FileLayoutMode,
    sortBy: FileSortBy,
    ascending: Boolean,
    foldersFirst: Boolean,
    onLayoutModeChange: (FileLayoutMode) -> Unit,
    onSortSelected: (FileSortBy) -> Unit,
    onAscendingChange: (Boolean) -> Unit,
    onFoldersFirstChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.compose_sort_and_view),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.view),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                FileLayoutMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = layoutMode == mode,
                        onClick = { onLayoutModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FileLayoutMode.entries.size
                        ),
                        modifier = Modifier.weight(1f),
                        label = { Text(layoutModeLabel(mode)) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.compose_sort_by),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(4.dp))
            FileSortBy.entries.forEach { option ->
                SortByRow(
                    option = option,
                    selected = sortBy == option,
                    onClick = { onSortSelected(option) }
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            SwitchRow(
                title = stringResource(R.string.file_list_action_sort_order_ascending),
                checked = ascending,
                onCheckedChange = onAscendingChange
            )
            SwitchRow(
                title = stringResource(R.string.file_list_action_sort_directories_first),
                checked = foldersFirst,
                onCheckedChange = onFoldersFirstChange
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SortByRow(
    option: FileSortBy,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = sortByLabel(option),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun layoutModeLabel(mode: FileLayoutMode): String = when (mode) {
    FileLayoutMode.LIST -> stringResource(R.string.file_list_action_view_list)
    FileLayoutMode.GRID -> stringResource(R.string.file_list_action_view_grid)
}

@Composable
private fun sortByLabel(option: FileSortBy): String = when (option) {
    FileSortBy.NAME -> stringResource(R.string.file_list_action_sort_by_name)
    FileSortBy.TYPE -> stringResource(R.string.file_list_action_sort_by_type)
    FileSortBy.SIZE -> stringResource(R.string.file_list_action_sort_by_size)
    FileSortBy.LAST_MODIFIED -> stringResource(R.string.file_list_action_sort_by_last_modified)
}
