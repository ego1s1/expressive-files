/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java8.nio.file.Paths
import java.util.Locale
import me.zhanghai.android.files.R
import me.zhanghai.android.files.filelist.OpenApkDefaultAction
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightMode
import me.zhanghai.android.files.theme.night.NightModeHelper

/** Applies a choice-dialog selection for [key]; unknown keys and values are ignored. */
fun applySettingChoice(key: String, value: String) {
    runCatching {
        when (key) {
            "appearance_night_mode" -> {
                val mode = NightMode.entries.getOrNull(value.toIntOrNull() ?: -1) ?: return
                Settings.NIGHT_MODE.putValue(mode)
                NightModeHelper.sync()
            }
            "appearance_ellipsize" -> {
                val ellipsize = TextUtils.TruncateAt.entries.getOrNull(value.toIntOrNull() ?: -1)
                    ?: return
                Settings.FILE_NAME_ELLIPSIZE.putValue(ellipsize)
            }
            "behavior_root_strategy" -> {
                val strategy = RootStrategy.entries.getOrNull(value.toIntOrNull() ?: -1) ?: return
                Settings.ROOT_STRATEGY.putValue(strategy)
            }
            "behavior_archive_encoding" -> {
                if (value.isNotBlank()) {
                    Settings.ARCHIVE_FILE_NAME_ENCODING.putValue(value)
                }
            }
            "behavior_open_apk" -> {
                val action = OpenApkDefaultAction.entries.getOrNull(value.toIntOrNull() ?: -1)
                    ?: return
                Settings.OPEN_APK_DEFAULT_ACTION.putValue(action)
            }
            "appearance_locale" -> applyLocale(value)
        }
    }
}

/** Applies a switch toggle for [key]; [checked] is the value before the tap. */
fun applySettingSwitch(key: String, checked: Boolean) {
    val newValue = !checked
    runCatching {
        when (key) {
            "appearance_black_theme" -> {
                Settings.BLACK_NIGHT_MODE.putValue(newValue)
                CustomThemeHelper.sync()
            }
            "appearance_dynamic_color" -> Settings.DYNAMIC_COLOR.putValue(newValue)
            "appearance_animations" -> Settings.FILE_LIST_ANIMATION.putValue(newValue)
            "behavior_show_hidden" -> Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(newValue)
            "behavior_pdf_thumbnail" -> Settings.SHOW_PDF_THUMBNAIL_PRE_28.putValue(newValue)
            "behavior_remote_thumbnail" ->
                Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.putValue(newValue)
        }
    }
}

/**
 * Applies an app-locale selection. [tag] is a BCP-47 tag or [SettingsStateBuilder.SYSTEM_DEFAULT_LOCALE].
 * The activity recreates automatically on pre-33; on 33+ the system settings own the locale.
 */
fun applyLocale(tag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return
    }
    runCatching {
        val locales = if (tag == SettingsStateBuilder.SYSTEM_DEFAULT_LOCALE) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.create(Locale.forLanguageTag(tag))
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}

/** Restores the default download-style folder used at launch. */
fun resetDefaultDirectory() {
    runCatching {
        @Suppress("DEPRECATION")
        putDefaultDirectory(Paths.get(Environment.getExternalStorageDirectory().absolutePath))
    }
}

/**
 * Restores every preference surfaced in the Compose settings screen to its default.
 * User data lists (storages, bookmarks, standard folders) are left untouched.
 */
fun resetSettingsToDefaults(context: Context) {
    val resources = context.resources
    fun booleanDefault(resId: Int): Boolean =
        runCatching { resources.getBoolean(resId) }.getOrDefault(false)
    runCatching {
        Settings.NIGHT_MODE.putValue(
            NightMode.entries[
                resources.getString(R.string.pref_default_value_night_mode).toInt()
            ]
        )
        Settings.BLACK_NIGHT_MODE.putValue(
            booleanDefault(R.bool.pref_default_value_black_night_mode)
        )
        Settings.FILE_LIST_ANIMATION.putValue(
            booleanDefault(R.bool.pref_default_value_file_list_animation)
        )
        Settings.DYNAMIC_COLOR.putValue(
            booleanDefault(R.bool.pref_default_value_dynamic_color)
        )
        Settings.FILE_NAME_ELLIPSIZE.putValue(
            TextUtils.TruncateAt.entries[
                resources.getString(R.string.pref_default_value_file_name_ellipsize).toInt()
            ]
        )
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(
            booleanDefault(R.bool.pref_default_value_file_list_show_hidden_files)
        )
        Settings.ROOT_STRATEGY.putValue(
            RootStrategy.entries[
                resources.getString(R.string.pref_default_value_root_strategy).toInt()
            ]
        )
        Settings.ARCHIVE_FILE_NAME_ENCODING.putValue(
            resources.getString(R.string.pref_default_value_archive_file_name_encoding)
        )
        Settings.OPEN_APK_DEFAULT_ACTION.putValue(
            OpenApkDefaultAction.entries[
                resources.getString(R.string.pref_default_value_open_apk_default_action).toInt()
            ]
        )
        Settings.SHOW_PDF_THUMBNAIL_PRE_28.putValue(
            booleanDefault(R.bool.pref_default_value_show_pdf_thumbnail_pre_28)
        )
        Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.putValue(
            booleanDefault(R.bool.pref_default_value_read_remote_files_for_thumbnail)
        )
        applyLocale(SettingsStateBuilder.SYSTEM_DEFAULT_LOCALE)
        CustomThemeHelper.sync()
        NightModeHelper.sync()
    }
    resetDefaultDirectory()
}
