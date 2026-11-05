/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R

/**
 * Bottom sheet with the file list's view options: sort/view, layout toggle, refresh and select all.
 *
 * The sheet is stateless: every row invokes its callback and then dismisses itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsSheet(
    isGrid: Boolean,
    onDismiss: () -> Unit,
    onSortClick: () -> Unit,
    onRefresh: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleLayout: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissAfter: (() -> Unit) -> Unit = { action ->
        action()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        // Default expanded shape: 28dp top corners.
        shape = BottomSheetDefaults.ExpandedShape,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.compose_view_options),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
            OptionRow(
                title = stringResource(R.string.compose_sort_and_view),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_expressive_sort),
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { dismissAfter(onSortClick) }
            )
            OptionRow(
                title = if (isGrid) {
                    stringResource(R.string.compose_switch_to_list)
                } else {
                    stringResource(R.string.compose_switch_to_grid)
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(
                            if (isGrid) {
                                R.drawable.ic_expressive_list
                            } else {
                                R.drawable.ic_expressive_grid
                            }
                        ),
                        contentDescription = null
                    )
                },
                onClick = { dismissAfter(onToggleLayout) }
            )
            OptionRow(
                title = stringResource(R.string.refresh),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null
                    )
                },
                onClick = { dismissAfter(onRefresh) }
            )
            OptionRow(
                title = stringResource(R.string.select_all),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null
                    )
                },
                onClick = { dismissAfter(onSelectAll) }
            )
        }
    }
}

@Composable
private fun OptionRow(
    title: String,
    leadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
private fun ListOptionsSheetPreview() {
    ListOptionsSheet(
        isGrid = false,
        onDismiss = {},
        onSortClick = {},
        onRefresh = {},
        onSelectAll = {},
        onToggleLayout = {}
    )
}
