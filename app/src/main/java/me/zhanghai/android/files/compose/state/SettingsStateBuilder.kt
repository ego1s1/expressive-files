/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.core.app.LocaleManagerCompat
import java.nio.charset.Charset
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.LocaleConfigCompat
import me.zhanghai.android.files.compose.model.SettingsChoiceOptionUi
import me.zhanghai.android.files.compose.model.SettingsItemKind
import me.zhanghai.android.files.compose.model.SettingsItemUi
import me.zhanghai.android.files.compose.model.SettingsSectionUi
import me.zhanghai.android.files.compose.model.SettingsUiState
import me.zhanghai.android.files.filelist.OpenApkDefaultAction
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.theme.night.NightMode
import me.zhanghai.android.files.util.toList
import me.zhanghai.android.files.util.valueCompat

/**
 * Builds the complete Compose settings model from [Settings].
 *
 * Every row of the legacy settings menu is represented: booleans are switches, enums and
 * value lists are choice dialogs, and the directory/storage managers are action rows that
 * the host wires to native Compose sub-screens. All reads are guarded because settings may
 * be unavailable in isolated tests or very early in process startup.
 */
object SettingsStateBuilder {
    fun build(context: Context, query: String = ""): SettingsUiState {
        val locale = readSetting { currentLocaleTag(context) }
        val supportedLocales = readSetting { supportedLocaleTags(context) }
        val nightMode = readSetting { Settings.NIGHT_MODE.valueCompat }
        val blackTheme = readSetting { Settings.BLACK_NIGHT_MODE.valueCompat }
        val animations = readSetting { Settings.FILE_LIST_ANIMATION.valueCompat }
        val dynamicColor = readSetting { Settings.DYNAMIC_COLOR.valueCompat }
        val ellipsize = readSetting { Settings.FILE_NAME_ELLIPSIZE.valueCompat }
        val showHidden = readSetting { Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat }
        val defaultDirectory = readSetting { Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat }
        val storages = readSetting { Settings.STORAGES.valueCompat }
        val standardDirs = readSetting { Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat }
        val bookmarks = readSetting { Settings.BOOKMARK_DIRECTORIES.valueCompat }
        val rootStrategy = readSetting { Settings.ROOT_STRATEGY.valueCompat }
        val archiveEncoding = readSetting { Settings.ARCHIVE_FILE_NAME_ENCODING.valueCompat }
        val openApk = readSetting { Settings.OPEN_APK_DEFAULT_ACTION.valueCompat }
        val pdfThumbnail =
            readSetting { Settings.SHOW_PDF_THUMBNAIL_PRE_28.valueCompat }
        val remoteThumbnail =
            readSetting { Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.valueCompat }

        val appearance = SettingsSectionUi(
            id = "appearance",
            title = context.getString(R.string.compose_appearance),
            items = listOf(
                localeItem(context, locale, supportedLocales),
                SettingsItemUi(
                    key = "appearance_night_mode",
                    title = context.getString(R.string.settings_night_mode_title),
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.CHOICE,
                    valueLabel = nightMode?.let {
                        nightModeEntries(context).getOrNull(it.ordinal)
                    },
                    enabled = nightMode != null,
                    options = NightMode.entries.map { mode ->
                        SettingsChoiceOptionUi(
                            value = mode.ordinal.toString(),
                            label = nightModeEntries(context).getOrElse(mode.ordinal) { mode.name },
                            selected = mode == nightMode
                        )
                    }
                ),
                SettingsItemUi(
                    key = "appearance_black_theme",
                    title = context.getString(R.string.settings_black_night_mode),
                    summary = context.getString(R.string.compose_use_pure_black_backgrounds_in_dark_mode),
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = blackTheme ?: false,
                    enabled = blackTheme != null
                ),
                SettingsItemUi(
                    key = "appearance_animations",
                    title = context.getString(R.string.settings_file_list_animation_title),
                    summary = context.getString(R.string.compose_animate_file_list_changes),
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = animations ?: false,
                    enabled = animations != null
                ),
                SettingsItemUi(
                    key = "appearance_dynamic_color",
                    title = context.getString(R.string.compose_dynamic_color),
                    summary = context.getString(R.string.compose_match_the_system_wallpaper),
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.SWITCH,
                    checked = dynamicColor ?: true,
                    enabled = dynamicColor != null
                ),
                SettingsItemUi(
                    key = "appearance_ellipsize",
                    title = context.getString(R.string.settings_file_name_ellipsize_title),
                    iconRes = R.drawable.settings_icon_white_24dp,
                    kind = SettingsItemKind.CHOICE,
                    valueLabel = ellipsize?.let {
                        ellipsizeEntries(context).getOrNull(it.ordinal)
                    },
                    enabled = ellipsize != null,
                    options = TextUtils.TruncateAt.entries.map { truncateAt ->
                        SettingsChoiceOptionUi(
                            value = truncateAt.ordinal.toString(),
                            label = ellipsizeEntries(context)
                                .getOrElse(truncateAt.ordinal) { truncateAt.name },
                            selected = truncateAt == ellipsize
                        )
                    }
                )
            )
        )

        val behavior = SettingsSectionUi(
            id = "behavior",
            title = context.getString(R.string.settings_behavior_title),
            items = buildList {
                add(
                    SettingsItemUi(
                        key = "behavior_show_hidden",
                        title = context.getString(R.string.file_list_action_show_hidden_files),
                        summary = context.getString(R.string.compose_show_dotfiles_and_hidden_folders),
                        iconRes = R.drawable.directory_icon_white_24dp,
                        kind = SettingsItemKind.SWITCH,
                        checked = showHidden ?: false,
                        enabled = showHidden != null
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_default_folder",
                        title = context.getString(R.string.settings_default_directory_title),
                        summary = context.getString(R.string.compose_choose_the_folder_that_opens_on_launch),
                        iconRes = R.drawable.directory_icon_white_24dp,
                        kind = SettingsItemKind.ACTION,
                        valueLabel = defaultDirectory?.toString(),
                        enabled = defaultDirectory != null
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_storages",
                        title = context.getString(R.string.settings_storages_title),
                        summary = context.getString(R.string.compose_manage_storage_locations),
                        iconRes = R.drawable.device_icon_white_24dp,
                        kind = SettingsItemKind.ACTION,
                        valueLabel = storages?.size?.toString(),
                        enabled = storages != null
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_standard_dirs",
                        title = context.getString(R.string.settings_standard_directories_title),
                        summary = context.getString(R.string.compose_enable_or_disable_standard_folders),
                        iconRes = R.drawable.directory_icon_white_24dp,
                        kind = SettingsItemKind.ACTION,
                        enabled = standardDirs != null
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_bookmarks",
                        title = context.getString(R.string.settings_bookmark_directories_title),
                        summary = context.getString(R.string.compose_manage_folders_pinned_to_the_drawer),
                        iconRes = R.drawable.directory_icon_white_24dp,
                        kind = SettingsItemKind.ACTION,
                        valueLabel = bookmarks?.size?.toString(),
                        enabled = bookmarks != null
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_root_strategy",
                        title = context.getString(R.string.settings_root_strategy_title),
                        summary = context.getString(R.string.compose_choose_how_root_file_access_is_requested),
                        iconRes = R.drawable.settings_icon_white_24dp,
                        kind = SettingsItemKind.CHOICE,
                        valueLabel = rootStrategy?.let {
                            rootStrategyEntries(context).getOrNull(it.ordinal)
                        },
                        enabled = rootStrategy != null,
                        options = RootStrategy.entries.map { strategy ->
                            SettingsChoiceOptionUi(
                                value = strategy.ordinal.toString(),
                                label = rootStrategyEntries(context)
                                    .getOrElse(strategy.ordinal) { strategy.name },
                                selected = strategy == rootStrategy
                            )
                        }
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_archive_encoding",
                        title = context.getString(R.string.settings_archive_file_name_encoding_title),
                        summary = context.getString(R.string.compose_encoding_used_for_archive_entry_names),
                        iconRes = R.drawable.settings_icon_white_24dp,
                        kind = SettingsItemKind.CHOICE,
                        valueLabel = archiveEncoding,
                        enabled = archiveEncoding != null,
                        options = Charset.availableCharsets().map { (name, charset) ->
                            SettingsChoiceOptionUi(
                                value = name,
                                label = charset.displayName(),
                                selected = name.equals(archiveEncoding, ignoreCase = true)
                            )
                        }
                    )
                )
                add(
                    SettingsItemUi(
                        key = "behavior_open_apk",
                        title = context.getString(R.string.settings_open_apk_default_action_title),
                        iconRes = R.drawable.settings_icon_white_24dp,
                        kind = SettingsItemKind.CHOICE,
                        valueLabel = openApk?.let {
                            openApkEntries(context).getOrNull(it.ordinal)
                        },
                        enabled = openApk != null,
                        options = OpenApkDefaultAction.entries.map { action ->
                            SettingsChoiceOptionUi(
                                value = action.ordinal.toString(),
                                label = openApkEntries(context)
                                    .getOrElse(action.ordinal) { action.name },
                                selected = action == openApk
                            )
                        }
                    )
                )
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    add(
                        SettingsItemUi(
                            key = "behavior_pdf_thumbnail",
                            title = context.getString(R.string.settings_show_pdf_thumbnail_pre_28_title),
                            summary = context.getString(R.string.settings_show_pdf_thumbnail_pre_28_summary),
                            iconRes = R.drawable.settings_icon_white_24dp,
                            kind = SettingsItemKind.SWITCH,
                            checked = pdfThumbnail ?: false,
                            enabled = pdfThumbnail != null
                        )
                    )
                }
                add(
                    SettingsItemUi(
                        key = "behavior_remote_thumbnail",
                        title = context.getString(R.string.settings_read_remote_files_for_thumbnail_title),
                        iconRes = R.drawable.settings_icon_white_24dp,
                        kind = SettingsItemKind.SWITCH,
                        checked = remoteThumbnail ?: false,
                        enabled = remoteThumbnail != null
                    )
                )
            }
        )

        val advanced = SettingsSectionUi(
            id = "advanced",
            title = context.getString(R.string.compose_advanced),
            items = listOf(
                SettingsItemUi(
                    key = "advanced_reset",
                    title = context.getString(R.string.compose_reset_preferences),
                    summary = context.getString(R.string.compose_restore_all_settings_to_their_defaults),
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

    private fun localeItem(
        context: Context,
        locale: String?,
        supportedLocales: List<Pair<String, String>>?
    ): SettingsItemUi {
        val systemDefault = context.getString(R.string.system_default)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SettingsItemUi(
                key = "appearance_locale",
                title = context.getString(R.string.settings_locale_title),
                iconRes = R.drawable.settings_icon_white_24dp,
                kind = SettingsItemKind.ACTION,
                valueLabel = locale?.let { tag ->
                    supportedLocales?.find { it.first == tag }?.second ?: tag
                } ?: systemDefault,
                enabled = locale != null
            )
        } else {
            val options = mutableListOf(
                SettingsChoiceOptionUi(
                    value = SYSTEM_DEFAULT_LOCALE,
                    label = systemDefault,
                    selected = locale == null || locale == SYSTEM_DEFAULT_LOCALE
                )
            )
            supportedLocales?.forEach { (tag, label) ->
                options.add(
                    SettingsChoiceOptionUi(
                        value = tag,
                        label = label,
                        selected = tag == locale
                    )
                )
            }
            SettingsItemUi(
                key = "appearance_locale",
                title = context.getString(R.string.settings_locale_title),
                iconRes = R.drawable.settings_icon_white_24dp,
                kind = SettingsItemKind.CHOICE,
                valueLabel = options.find { it.selected }?.label,
                enabled = locale != null,
                options = options
            )
        }
    }

    private fun currentLocaleTag(context: Context): String? = runCatching {
        LocaleManagerCompat.getApplicationLocales(context).toList().firstOrNull()
            ?.toLanguageTag() ?: SYSTEM_DEFAULT_LOCALE
    }.getOrNull()

    private fun supportedLocaleTags(context: Context): List<Pair<String, String>>? = runCatching {
        LocaleConfigCompat(context).supportedLocales?.toList()
            ?.sortedBy { it.toLanguageTag() }
            ?.map { locale: java.util.Locale ->
                val tag = locale.toLanguageTag()
                val label = locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
                tag to label
            }
    }.getOrNull()

    private fun nightModeEntries(context: Context): List<String> = runCatching {
        context.resources.getStringArray(R.array.settings_night_mode_entries).toList()
    }.getOrDefault(emptyList())

    private fun ellipsizeEntries(context: Context): List<String> = runCatching {
        context.resources.getStringArray(R.array.settings_file_name_ellipsize_entries).toList()
    }.getOrDefault(emptyList())

    private fun rootStrategyEntries(context: Context): List<String> = runCatching {
        context.resources.getStringArray(R.array.settings_root_strategy_entries).toList()
    }.getOrDefault(emptyList())

    private fun openApkEntries(context: Context): List<String> = runCatching {
        context.resources.getStringArray(R.array.settings_open_apk_default_action_entries).toList()
    }.getOrDefault(emptyList())

    const val SYSTEM_DEFAULT_LOCALE = "system"
}

/** Puts [path] as the default folder, keeping the previous value on failure. */
fun putDefaultDirectory(path: Path) {
    runCatching { Settings.FILE_LIST_DEFAULT_DIRECTORY.putValue(path) }
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

private fun <T> readSetting(block: () -> T): T? =
    runCatching { block() }.getOrNull()

/** Convenience alias for call sites that use function naming rather than the object. */
fun buildSettingsUiState(context: Context, query: String = ""): SettingsUiState =
    SettingsStateBuilder.build(context, query)
