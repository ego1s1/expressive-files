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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileListUiState

private val TOP_BAR_HEIGHT = 64.dp

/**
 * Compact file-list header. It intentionally uses the same surface color as the list so the
 * header blends into the content instead of reading as a separate band.
 */
@Composable
fun FileListTopBar(
    state: FileListUiState,
    onNavigateUp: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val title = state.title.ifBlank { "Files" } // TODO: localize
    val subtitle = state.subtitle

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(TOP_BAR_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.canNavigateUp) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate up" // TODO: localize
                    )
                }
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 26.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_search),
                    contentDescription = "Search files", // TODO: localize
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSortClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_sort),
                    contentDescription = "Sort and view, sorted by ${state.sortLabel}", // TODO: localize
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vertical_icon_white_24dp),
                    contentDescription = "View options", // TODO: localize
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FileListTopBarPreview() {
    FileListTopBar(
        state = FileListUiState(
            title = "Downloads",
            subtitle = "12 items • 4.2 GB free", // TODO: localize
            canNavigateUp = false
        ),
        onNavigateUp = {},
        onSearchClick = {},
        onSortClick = {},
        onOptionsClick = {}
    )
}
