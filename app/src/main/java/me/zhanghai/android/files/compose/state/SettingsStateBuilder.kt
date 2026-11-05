/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.SettingsItemKind
import me.zhanghai.android.files.compose.model.SettingsItemUi
import me.zhanghai.android.files.compose.model.SettingsSectionUi
import me.zhanghai.android.files.compose.model.SettingsUiState
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

/**
 * Builds a compact Compose settings model.
 *
 * Only the three cheap, observable booleans are read from [Settings]; rows that need a real
 * preference screen (theme picker, default folder, root mode, ...) stay as action placeholders for
 * the integrator to wire later. All reads are guarded because settings may be unavailable in
 * isolated tests or very early in process startup.
 */
object SettingsStateBuilder {
    fun build(context: Context, query: String = ""): SettingsUiState {
        val blackTheme = readBooleanSetting { Settings.BLACK_NIGHT_MODE.valueCompat }
        val animations = readBooleanSetting { Settings.FILE_LIST_ANIMATION.valueCompat }
        val dynamicColor = readBooleanSetting { Settings.DYNAMIC_COLOR.valueCompat }
        val showHidden = readBooleanSetting { Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat }

        val appearance = SettingsSectionUi(
            id = "appearance",
            title = "Appearance", // TODO: localize
            items = listOf(
                SettingsItemUi(
                    key = "appearance_black_theme",
                    title = context.getString(R.string.settings_black_night_mode),
                    summary = "Use pure black backgrounds in dark mode", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = blackTheme ?: false,
                    enabled = blackTheme != null
                ),
                SettingsItemUi(
                    key = "appearance_animations",
                    title = context.getString(R.string.settings_file_list_animation_title),
                    summary = "Animate file list changes", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = animations ?: false,
                    enabled = animations != null
                ),
                SettingsItemUi(
                    key = "appearance_dynamic_color",
                    title = "Dynamic color", // TODO: localize
                    summary = "Match the system wallpaper", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = dynamicColor ?: true,
                    enabled = dynamicColor != null
                ),
                SettingsItemUi(
                    key = "appearance_theme",
                    title = context.getString(R.string.settings_theme_color_title),
                    summary = "Choose the app accent color", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.ACTION,
                    valueLabel = "System default" // TODO: localize
                )
            )
        )

        val behavior = SettingsSectionUi(
            id = "behavior",
            title = context.getString(R.string.settings_behavior_title),
            items = listOf(
                SettingsItemUi(
                    key = "behavior_show_hidden",
                    title = context.getString(R.string.file_list_action_show_hidden_files),
                    summary = "Show dotfiles and hidden folders", // TODO: localize
                    iconRes = R.drawable.directory_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = showHidden ?: false,
                    enabled = showHidden != null
                ),
                SettingsItemUi(
                    key = "behavior_default_folder",
                    title = context.getString(R.string.settings_default_directory_title),
                    summary = "Choose the folder that opens on launch", // TODO: localize
                    iconRes = R.drawable.directory_icon_white_24dp,
                    kind = SettingsItemKind.ACTION
                ),
                SettingsItemUi(
                    key = "behavior_bookmarks",
                    title = context.getString(R.string.settings_bookmark_directories_title),
                    summary = "Manage folders pinned to the drawer", // TODO: localize
                    iconRes = R.drawable.directory_icon_white_24dp,
                    kind = SettingsItemKind.ACTION
                )
            )
        )

        val advanced = SettingsSectionUi(
            id = "advanced",
            title = "Advanced", // TODO: localize
            items = listOf(
                SettingsItemUi(
                    key = "advanced_root_mode",
                    title = context.getString(R.string.settings_root_strategy_title),
                    summary = "Choose how root file access is requested", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.ACTION
                ),
                SettingsItemUi(
                    key = "advanced_archive_encoding",
                    title = context.getString(R.string.settings_archive_file_name_encoding_title),
                    summary = "Encoding used for archive entry names", // TODO: localize
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.ACTION
                ),
                SettingsItemUi(
                    key = "advanced_reset",
                    title = "Reset preferences", // TODO: localize
                    summary = "Restore all settings to their defaults", // TODO: localize
                    iconRes = R.drawable.reset_icon_white_24dp,
                    kind = SettingsItemKind.ACTION
                )
            )
        )

        val sections = listOf(appearance, behavior, advanced)
            .map { it.filter(query) }
            .filter { it.items.isNotEmpty() }
        return SettingsUiState(sections = sections, query = query)
    }
}

private fun SettingsSectionUi.filter(query: String): SettingsSectionUi {
    if (query.isBlank()) {
        return this
    }
    return copy(
        items = items.filter { item ->
            item.title.contains(query, ignoreCase = true) ||
                item.summary?.contains(query, ignoreCase = true) == true ||
                item.valueLabel?.contains(query, ignoreCase = true) == true
        }
    )
}

private fun readBooleanSetting(block: () -> Boolean): Boolean? =
    runCatching { block() }.getOrNull()

/** Convenience alias for call sites that use function naming rather than the object. */
fun buildSettingsUiState(context: Context, query: String = ""): SettingsUiState =
    SettingsStateBuilder.build(context, query)
