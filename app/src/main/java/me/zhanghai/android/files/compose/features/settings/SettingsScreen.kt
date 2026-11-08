/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.settings

import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.theme.ExpressiveFilesPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.compose.model.SettingsItemKind
import me.zhanghai.android.files.compose.model.SettingsItemUi
import me.zhanghai.android.files.compose.model.SettingsSectionUi
import me.zhanghai.android.files.compose.model.SettingsUiState

/**
 * Searchable settings screen. Filtering happens locally over [state]; the host
 * is responsible for persisting taps through [onItemClick].
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onQueryChange: (String) -> Unit,
    onItemClick: (SettingsItemUi) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    val query = state.query.trim()
    val sections = filterSettings(state.sections, query)
    val listState = rememberLazyListState()
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        SettingsTopBar(
            query = state.query,
            onQueryChange = onQueryChange,
            onBack = onBack
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            if (sections.isEmpty()) {
                item(key = "empty-state") {
                    SettingsEmptyState(isSearching = query.isNotEmpty())
                }
            } else {
                sections.forEach { section ->
                    item(key = "header-${section.id}") {
                        SettingsSectionHeader(
                            title = section.title,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    itemsIndexed(
                        items = section.items,
                        key = { _, item -> "${section.id}/${item.key}" }
                    ) { index, item ->
                        Box(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp
                            )
                        ) {
                            SettingsRow(
                                item = item,
                                shape = settingsRowShape(index, section.items.size),
                                onClick = { onItemClick(item) }
                            )
                        }
                    }
                    item(key = "gap-${section.id}") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            item(key = "about") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onAbout) {
                        Text(stringResource(R.string.about_title))
                    }
                }
            }
        }
    }
}

private fun filterSettings(
    sections: List<SettingsSectionUi>,
    query: String
): List<SettingsSectionUi> {
    if (query.isEmpty()) {
        return sections
    }
    return sections.mapNotNull { section ->
        val items = section.items.filter { item ->
            item.title.contains(query, ignoreCase = true) ||
                item.summary?.contains(query, ignoreCase = true) == true
        }
        if (items.isEmpty()) null else section.copy(items = items)
    }
}

private val PreviewSettingsState = SettingsUiState(
    sections = listOf(
        SettingsSectionUi(
            id = "appearance",
            title = "Appearance",
            items = listOf(
                SettingsItemUi(
                    key = "theme",
                    title = "Theme",
                    summary = "Follow system",
                    kind = SettingsItemKind.CHOICE,
                    valueLabel = "System"
                ),
                SettingsItemUi(
                    key = "accent",
                    title = "Accent color",
                    kind = SettingsItemKind.CHOICE,
                    valueLabel = "Dynamic"
                ),
                SettingsItemUi(
                    key = "grid",
                    title = "Use grid view by default",
                    kind = SettingsItemKind.SWITCH,
                    checked = true
                )
            )
        ),
        SettingsSectionUi(
            id = "files",
            title = "Files",
            items = listOf(
                SettingsItemUi(
                    key = "hidden",
                    title = "Show hidden files",
                    summary = "Files starting with a dot",
                    kind = SettingsItemKind.SWITCH,
                    checked = false
                ),
                SettingsItemUi(
                    key = "thumbnails",
                    title = "Thumbnails",
                    kind = SettingsItemKind.CHOICE,
                    valueLabel = "Wi-Fi only"
                ),
                SettingsItemUi(
                    key = "storage",
                    title = "Storage locations",
                    kind = SettingsItemKind.ACTION
                )
            )
        ),
        SettingsSectionUi(
            id = "about",
            title = "About",
            items = listOf(
                SettingsItemUi(
                    key = "about",
                    title = "About Expressive Files",
                    kind = SettingsItemKind.ACTION
                )
            )
        )
    )
)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ExpressiveFilesPreview {
        SettingsScreen(
            state = PreviewSettingsState,
            onQueryChange = {},
            onItemClick = {},
            onBack = {},
            onAbout = {}
        )
    }
}
