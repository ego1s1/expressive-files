/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

/**
 * Secondary selection actions. The contextual bar keeps the primary actions visible and
 * delegates "more" here so a misc-tap never silently selects every file.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionOptionsSheet(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "$selectedCount selected", // TODO: localize
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SelectionOptionRow(
                label = "Select all", // TODO: localize
                icon = Icons.Filled.Check,
                onClick = {
                    onSelectAll()
                    onDismiss()
                }
            )
            SelectionOptionRow(
                label = "Deselect all", // TODO: localize
                icon = Icons.Filled.Clear,
                onClick = {
                    onDeselectAll()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun SelectionOptionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectionOptionsSheetPreview() {
    ExpressiveFilesPreview {
        SelectionOptionsSheet(
            selectedCount = 3,
            onDismiss = {},
            onSelectAll = {},
            onDeselectAll = {}
        )
    }
}
