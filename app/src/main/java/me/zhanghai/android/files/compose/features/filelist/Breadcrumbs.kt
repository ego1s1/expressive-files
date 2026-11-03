/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.BreadcrumbUi

private const val BREADCRUMB_ROW_HEIGHT_DP = 48
private const val BREADCRUMB_FADE_WIDTH_DP = 24

/**
 * A horizontally scrolling breadcrumb trail with an emphasized current location.
 *
 * The trail automatically scrolls the current breadcrumb into view and fades its edges while
 * there is more content in either direction.
 */
@Composable
fun Breadcrumbs(
    breadcrumbs: List<BreadcrumbUi>,
    onBreadcrumbClick: (BreadcrumbUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val currentIndex = breadcrumbs.indexOfFirst { it.isCurrent }
    val canScrollBackward = listState.canScrollBackward
    val canScrollForward = listState.canScrollForward

    LaunchedEffect(breadcrumbs) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BREADCRUMB_ROW_HEIGHT_DP.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp),
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
}

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
