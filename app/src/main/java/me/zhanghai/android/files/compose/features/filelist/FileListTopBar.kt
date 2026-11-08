/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.FileListUiState
import me.zhanghai.android.files.compose.theme.ExpressiveAccelerateEasing
import me.zhanghai.android.files.compose.theme.ExpressiveDecelerateEasing

private val TOP_BAR_MIN_HEIGHT = 64.dp

/**
 * Compact file-list header. It uses the same surface color as the list so the header
 * blends into the content instead of reading as a separate band.
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
    val title = state.title.ifBlank { stringResource(R.string.file_list_title) }
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
                .heightIn(min = TOP_BAR_MIN_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.canNavigateUp) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.compose_navigate_up)
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
                // Crossfade the chrome on folder change: the title resolves from the
                // breadcrumb trail while the list is still loading, so a hard swap flashed
                // the old name over the new folder. A short fade keeps the header calm.
                AnimatedContent(
                    targetState = title to subtitle,
                    transitionSpec = {
                        fadeIn(tween(150, easing = ExpressiveDecelerateEasing)) togetherWith
                            fadeOut(tween(120, easing = ExpressiveAccelerateEasing))
                    },
                    label = "fileListChrome"
                ) { (animatedTitle, animatedSubtitle) ->
                    Column {
                        Text(
                            text = animatedTitle,
                            color = colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!animatedSubtitle.isNullOrBlank()) {
                            Text(
                                text = animatedSubtitle,
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_search),
                    contentDescription = stringResource(R.string.compose_search_files),
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSortClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_sort),
                    contentDescription = stringResource(
                        R.string.compose_sort_and_view_sorted_by,
                        state.sortLabel
                    ),
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vertical_icon_white_24dp),
                    contentDescription = stringResource(R.string.compose_view_options),
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
            subtitle = stringResource(R.string.compose_12_items_4_2_gb_free),
            canNavigateUp = false
        ),
        onNavigateUp = {},
        onSearchClick = {},
        onSortClick = {},
        onOptionsClick = {}
    )
}
