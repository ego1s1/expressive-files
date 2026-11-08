/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.BreadcrumbUi

private val BREADCRUMB_ROW_MIN_HEIGHT = 48.dp
private val BREADCRUMB_LABEL_MAX_WIDTH = 220.dp

/**
 * Full ancestor trail ending in the current folder. The current crumb is highlighted and
 * not clickable; the app-bar title still owns the accessible heading. Plain text and
 * chevrons only — no chips, pills, or action buttons.
 */
@Composable
fun Breadcrumbs(
    breadcrumbs: List<BreadcrumbUi>,
    onBreadcrumbClick: (BreadcrumbUi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (breadcrumbs.isEmpty()) {
        return
    }
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    LaunchedEffect(breadcrumbs) {
        listState.animateScrollToItem(breadcrumbs.lastIndex)
    }
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BREADCRUMB_ROW_MIN_HEIGHT),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = breadcrumbs,
            key = { _, crumb -> crumb.path }
        ) { index, crumb ->
            val isCurrent = crumb.isCurrent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = crumb.label,
                    modifier = Modifier
                        .widthIn(max = BREADCRUMB_LABEL_MAX_WIDTH)
                        .then(
                            if (isCurrent) {
                                Modifier
                            } else {
                                Modifier.clickable(
                                    onClickLabel = stringResource(R.string.compose_open_folder)
                                ) {
                                    onBreadcrumbClick(crumb)
                                }
                            }
                        )
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    color = if (isCurrent) {
                        colorScheme.onSurface
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    style = if (isCurrent) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (index < breadcrumbs.lastIndex) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
