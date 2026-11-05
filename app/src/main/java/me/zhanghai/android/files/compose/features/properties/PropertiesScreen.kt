/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.properties

import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import me.zhanghai.android.files.compose.icons.ExpressiveIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.PropertiesUiState
import me.zhanghai.android.files.compose.model.PropertyRowUi
import me.zhanghai.android.files.compose.model.PropertySectionUi

private const val BACK = "Back" // TODO: localize
private const val PROPERTIES = "Properties" // TODO: localize
private const val EMPTY = "No properties to show" // TODO: localize

/**
 * Stateless properties browser: a file header, tabbed property groups and copyable rows.
 */
@Composable
fun PropertiesScreen(
    state: PropertiesUiState,
    onTabSelected: (Int) -> Unit,
    onCopy: (String) -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PropertiesTopBar(onBack = onBack)
            PropertiesHeader(state = state)
            if (state.tabTitles.isNotEmpty()) {
                PropertiesTabs(
                    titles = state.tabTitles,
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected
                )
            }
            when {
                state.isLoading -> PropertiesLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                state.error != null -> PropertiesError(
                    message = state.error.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                else -> PropertiesContent(
                    sections = state.sections,
                    onCopy = onCopy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PropertiesTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = 64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = BACK
            )
        }
        Text(
            text = PROPERTIES,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PropertiesHeader(state: PropertiesUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val iconModifier = Modifier.size(28.dp)
                if (state.iconRes != 0) {
                    Icon(
                        painter = painterResource(state.iconRes),
                        contentDescription = null,
                        modifier = iconModifier
                    )
                } else {
                    Icon(
                        imageVector = ExpressiveIcons.Folder,
                        contentDescription = null,
                        modifier = iconModifier
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = state.subtitle
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.StartEllipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertiesTabs(
    titles: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val selectedIndex = selectedTab.coerceIn(0, titles.lastIndex)
    val tabContent: @Composable () -> Unit = {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                modifier = Modifier.heightIn(min = 48.dp),
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
    if (titles.size <= 3) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 0.dp,
            tabs = tabContent
        )
    } else {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 0.dp,
            tabs = tabContent
        )
    }
}

@Composable
private fun PropertiesContent(
    sections: List<PropertySectionUi>,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sections.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = EMPTY,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }
    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = sections,
            key = { index, section -> "$index:${section.title}" }
        ) { _, section ->
            PropertySectionCard(
                section = section,
                onCopy = onCopy
            )
        }
    }
}

@Composable
private fun PropertySectionCard(
    section: PropertySectionUi,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (section.title.isNotBlank()) {
                Text(
                    text = section.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            section.rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                PropertyRow(row = row, onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun PropertyRow(
    row: PropertyRowUi,
    onCopy: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.label,
            modifier = Modifier
                .weight(0.4f)
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = row.value,
            modifier = Modifier
                .weight(0.6f)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        if (row.copyable) {
            IconButton(onClick = { onCopy(row.value) }) {
                Icon(
                    painter = painterResource(R.drawable.copy_icon_control_normal_24dp),
                    contentDescription = "Copy ${row.label}", // TODO: localize
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}

private val PreviewPropertiesState = PropertiesUiState(
    title = "IMG_2026_10_31.jpg",
    subtitle = "DCIM/Camera \u00B7 4.2 MB",
    tabTitles = listOf("General", "Image", "Permissions"),
    selectedTab = 0,
    sections = listOf(
        PropertySectionUi(
            title = "File",
            rows = listOf(
                PropertyRowUi("Name", "IMG_2026_10_31.jpg", copyable = true),
                PropertyRowUi(
                    "Path",
                    "/storage/emulated/0/DCIM/Camera/IMG_2026_10_31.jpg",
                    copyable = true
                ),
                PropertyRowUi("Size", "4.2 MB"),
                PropertyRowUi("Modified", "Oct 31, 2026, 14:02")
            )
        ),
        PropertySectionUi(
            title = "Image",
            rows = listOf(
                PropertyRowUi("Dimensions", "4032 x 3024"),
                PropertyRowUi("Format", "JPEG")
            )
        )
    )
)

@Preview(showBackground = true)
@Composable
private fun PropertiesScreenPreview() {
    ExpressiveFilesPreview {
        PropertiesScreen(
            state = PreviewPropertiesState,
            onTabSelected = {},
            onCopy = {},
            onBack = {}
        )
    }
}
