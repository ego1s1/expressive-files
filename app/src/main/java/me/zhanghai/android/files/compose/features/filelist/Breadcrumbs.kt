/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.BreadcrumbUi

private const val BREADCRUMB_ROW_HEIGHT_DP = 48
private const val BREADCRUMB_FADE_WIDTH_DP = 24
private const val BREADCRUMB_SORT_CHIP_HEIGHT_DP = 32
private const val BREADCRUMB_TRAILING_GAP_DP = 8
private const val BREADCRUMB_SORT_LABEL_MAX_WIDTH_DP = 160

/**
 * A horizontally scrolling breadcrumb trail with an emphasized current location.
 *
 * The trail automatically scrolls the current breadcrumb into view and fades its edges while
 * there is more content in either direction.
 *
 * Optional trailing controls (sort chip and list options) live on the same 48dp row. The trail is
 * weighted, so it gives up width to the controls instead of pushing them off-screen; its scroll
 * fades stay attached to the scrollable region rather than to the full row.
 */
@Composable
fun Breadcrumbs(
    breadcrumbs: List<BreadcrumbUi>,
    onBreadcrumbClick: (BreadcrumbUi) -> Unit,
    modifier: Modifier = Modifier,
    sortLabel: String? = null,
    onSortClick: (() -> Unit)? = null,
    onOptionsClick: (() -> Unit)? = null,
    isGrid: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val currentIndex = breadcrumbs.indexOfFirst { it.isCurrent }
    val canScrollBackward = listState.canScrollBackward
    val canScrollForward = listState.canScrollForward
    val hasTrailingControls = onSortClick != null || onOptionsClick != null

    LaunchedEffect(breadcrumbs) {
        if (currentIndex >= 0) {
            if (breadcrumbs.size <= 2) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(currentIndex)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BREADCRUMB_ROW_HEIGHT_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = breadcrumbs,
                    key = { _, breadcrumb -> breadcrumb.path }
                ) { index, breadcrumb ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BreadcrumbItem(
                            breadcrumb = breadcrumb,
                            onClick = { onBreadcrumbClick(breadcrumb) }
                        )
                        if (index < breadcrumbs.lastIndex) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(16.dp),
                                tint = colorScheme.outline
                            )
                        }
                    }
                }
            }
            if (canScrollBackward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(BREADCRUMB_FADE_WIDTH_DP.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(colorScheme.surface, Color.Transparent)
                            )
                        )
                )
            }
            if (canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(BREADCRUMB_FADE_WIDTH_DP.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, colorScheme.surface)
                            )
                        )
                )
            }
        }
        if (hasTrailingControls) {
            Spacer(Modifier.width(BREADCRUMB_TRAILING_GAP_DP.dp))
            if (onSortClick != null) {
                BreadcrumbSortChip(
                    label = sortLabel?.takeIf { it.isNotBlank() } ?: "Sort", // TODO: localize
                    isGrid = isGrid,
                    onClick = onSortClick
                )
                if (onOptionsClick != null) {
                    Spacer(Modifier.width(BREADCRUMB_TRAILING_GAP_DP.dp))
                }
            }
            if (onOptionsClick != null) {
                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.size(BREADCRUMB_ROW_HEIGHT_DP.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vertical_icon_white_24dp),
                        contentDescription = "List options", // TODO: localize
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(BREADCRUMB_TRAILING_GAP_DP.dp))
        }
    }
}

/**
 * A compact 32dp sort chip with a 48dp touch target.
 *
 * The [isGrid] flag only swaps the leading glyph so the same control reads as "currently grid"
 * while the label still describes the active sort order.
 */
@Composable
private fun BreadcrumbSortChip(
    label: String,
    isGrid: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val icon = painterResource(
        if (isGrid) R.drawable.ic_expressive_grid else R.drawable.ic_expressive_sort
    )

    Box(
        modifier = Modifier
            .height(BREADCRUMB_ROW_HEIGHT_DP.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                onClickLabel = "Change sort", // TODO: localize
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(colorScheme.surfaceContainerHighest)
                .border(1.dp, colorScheme.outlineVariant, MaterialTheme.shapes.small),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = "Sort and view, sorted by $label", // TODO: localize
                modifier = Modifier.size(18.dp),
                tint = colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "Breadcrumbs - list", showBackground = true)
@Composable
private fun BreadcrumbsListPreview() {
    MaterialTheme {
        Breadcrumbs(
            breadcrumbs = previewBreadcrumbs(),
            onBreadcrumbClick = {},
            sortLabel = "Name (A to Z)",
            onSortClick = {},
            onOptionsClick = {}
        )
    }
}

@Preview(name = "Breadcrumbs - grid", showBackground = true)
@Composable
private fun BreadcrumbsGridPreview() {
    MaterialTheme {
        Breadcrumbs(
            breadcrumbs = previewBreadcrumbs(),
            onBreadcrumbClick = {},
            sortLabel = "Date modified",
            onSortClick = {},
            onOptionsClick = {},
            isGrid = true
        )
    }
}

private fun previewBreadcrumbs(): List<BreadcrumbUi> = listOf(
    BreadcrumbUi(
        path = "/storage/emulated/0",
        label = "Internal storage"
    ),
    BreadcrumbUi(
        path = "/storage/emulated/0/Documents",
        label = "Documents"
    ),
    BreadcrumbUi(
        path = "/storage/emulated/0/Documents/Projects",
        label = "Projects",
        isCurrent = true
    )
)

@Composable
private fun BreadcrumbItem(
    breadcrumb: BreadcrumbUi,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (breadcrumb.isCurrent) {
        colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (breadcrumb.isCurrent) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .height(BREADCRUMB_ROW_HEIGHT_DP.dp)
            .widthIn(min = 48.dp)
            .clip(CircleShape)
            .clickable(
                onClickLabel = "Go to ${breadcrumb.label}", // TODO: localize
                role = Role.Button,
                onClick = onClick
            )
            .semantics { selected = breadcrumb.isCurrent },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .background(containerColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = breadcrumb.label,
                modifier = Modifier.widthIn(max = 220.dp),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (breadcrumb.isCurrent) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
