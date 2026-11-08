/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.model

import androidx.annotation.DrawableRes

enum class FileLayoutMode { LIST, GRID }

/** Immutable row model shared by the Compose file list and the legacy adapters. */
data class FileEntryUi(
    val path: String,
    val name: String,
    val supportingText: String? = null,
    val isDirectory: Boolean = false,
    val isSelected: Boolean = false,
    val isSymlink: Boolean = false,
    val isHidden: Boolean = false,
    val isArchive: Boolean = false,
    @DrawableRes val iconRes: Int = 0,
    val thumbnailKey: String? = null,
    val badgeText: String? = null
)

data class BreadcrumbUi(
    val path: String,
    val label: String,
    val isCurrent: Boolean = false
)

data class FileListUiState(
    val title: String = "",
    val subtitle: String? = null,
    val breadcrumbs: List<BreadcrumbUi> = emptyList(),
    val entries: List<FileEntryUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val emptyTitle: String? = null,
    val emptyBody: String? = null,
    val selectedCount: Int = 0,
    val layoutMode: FileLayoutMode = FileLayoutMode.LIST,
    val sortLabel: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val canPaste: Boolean = false,
    val canNavigateUp: Boolean = false
)

data class DrawerEntryUi(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    @DrawableRes val iconRes: Int = 0,
    val selected: Boolean = false,
    val badge: String? = null,
    val isAction: Boolean = false
)

data class DrawerSectionUi(
    val id: String,
    val title: String? = null,
    val entries: List<DrawerEntryUi> = emptyList()
)

data class NavigationUiState(
    val appName: String = "Expressive Files",
    val version: String = "",
    val sections: List<DrawerSectionUi> = emptyList()
)

data class ShortcutEntryUi(
    val path: String,
    val name: String,
    val subtitle: String? = null,
    val isDirectory: Boolean = false,
    val isFavorite: Boolean = false,
    @DrawableRes val iconRes: Int = 0
)

data class HomeUiState(
    val recents: List<ShortcutEntryUi> = emptyList(),
    val favorites: List<ShortcutEntryUi> = emptyList(),
    val isLoading: Boolean = false
)

data class HomeSectionUi(
    val id: String,
    val title: String,
    val entries: List<ShortcutEntryUi>
)

enum class SettingsItemKind { SWITCH, ACTION, CHOICE, SLIDER }

data class SettingsItemUi(
    val key: String,
    val title: String,
    val summary: String? = null,
    @DrawableRes val iconRes: Int = 0,
    val kind: SettingsItemKind = SettingsItemKind.ACTION,
    val checked: Boolean? = null,
    val valueLabel: String? = null,
    val enabled: Boolean = true,
    val options: List<SettingsChoiceOptionUi> = emptyList()
)

/** A single option in a [SettingsItemKind.CHOICE] dialog. [value] is opaque to the UI. */
data class SettingsChoiceOptionUi(
    val value: String,
    val label: String,
    val selected: Boolean = false,
    val colorArgb: Int? = null
)

data class SettingsSectionUi(
    val id: String,
    val title: String,
    val items: List<SettingsItemUi>
)

data class SettingsUiState(
    val sections: List<SettingsSectionUi> = emptyList(),
    val query: String = ""
)

data class OperationUi(
    val id: String,
    val title: String,
    val detail: String? = null,
    val progress: Float? = null,
    val canCancel: Boolean = true,
    val isComplete: Boolean = false,
    val error: String? = null
)

data class ConflictUi(
    val fileName: String,
    val existingName: String,
    val applyToAllAllowed: Boolean = true
)

data class PropertyRowUi(
    val label: String,
    val value: String,
    val copyable: Boolean = false
)

data class PropertySectionUi(
    val title: String,
    val rows: List<PropertyRowUi>
)

data class PropertiesUiState(
    val title: String = "",
    val subtitle: String? = null,
    val iconRes: Int = 0,
    val tabTitles: List<String> = emptyList(),
    val selectedTab: Int = 0,
    val sections: List<PropertySectionUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
