/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.compose.model.ShortcutEntryUi
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.filelist.name
import org.json.JSONArray
import org.json.JSONObject

/**
 * Process-wide persistence for recently opened files and folders.
 *
 * Recents are kept most-recent-first, de-duplicated by path and capped so the drawer/home screen
 * stays cheap to render. As with [FavoritesStore], `RecentsStore(context)` is supported via the
 * `invoke` operator while the plain object form avoids passing a Context through the UI.
 */
object RecentsStore {
    private const val PREFERENCES_NAME = "expressive_recents"
    private const val KEY_ENTRIES = "entries"
    private const val MAX_ENTRIES = 50

    private val lock = Any()

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var isLoaded = false

    private val _recents = MutableStateFlow<List<ShortcutEntryUi>>(emptyList())
    private val recentsState = _recents.asStateFlow()

    val recents: StateFlow<List<ShortcutEntryUi>>
        get() {
            ensureLoaded()
            return recentsState
        }

    operator fun invoke(context: Context): RecentsStore {
        initialize(context)
        return this
    }

    /** Binds this store to the process application context and loads persisted entries once. */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        ensureLoaded()
    }

    /** Records an opened item, moving it to the front if it was already present. */
    fun record(item: FileItem) {
        synchronized(lock) {
            ensureLoaded()
            val itemPath = item.path.toString()
            val updated = (listOf(item.toRecentEntryUi()) +
                _recents.value.filterNot { it.path == itemPath })
                .take(MAX_ENTRIES)
            _recents.value = updated
            persist(updated)
        }
    }

    fun clear() {
        synchronized(lock) {
            _recents.value = emptyList()
            runCatching {
                preferences().edit().remove(KEY_ENTRIES).apply()
            }
        }
    }

    private fun ensureLoaded() {
        if (isLoaded) {
            return
        }
        synchronized(lock) {
            if (isLoaded) {
                return
            }
            val loaded = load()
            if (loaded != null) {
                _recents.value = loaded
                isLoaded = true
            }
        }
    }

    private fun preferences(): SharedPreferences {
        val context = appContext ?: application.applicationContext
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun load(): List<ShortcutEntryUi>? = runCatching {
        parse(preferences().getString(KEY_ENTRIES, null))
    }.getOrNull()

    private fun persist(entries: List<ShortcutEntryUi>) {
        runCatching {
            preferences().edit().putString(KEY_ENTRIES, serialize(entries)).apply()
        }
    }

    private fun parse(raw: String?): List<ShortcutEntryUi> {
        if (raw.isNullOrEmpty()) {
            return emptyList()
        }
        val array = JSONArray(raw)
        val seenPaths = mutableSetOf<String>()
        val result = mutableListOf<ShortcutEntryUi>()
        for (index in 0 until array.length()) {
            if (result.size >= MAX_ENTRIES) {
                break
            }
            val entry = array.optJSONObject(index) ?: continue
            val path = entry.optString("path")
            if (path.isEmpty() || !seenPaths.add(path)) {
                continue
            }
            val name = entry.optString("name").takeIf { it.isNotEmpty() }
                ?: path.substringAfterLast('/').takeIf { it.isNotEmpty() }
                ?: path
            val isDirectory = entry.optBoolean("isDirectory", false)
            result.add(
                ShortcutEntryUi(
                    path = path,
                    name = name,
                    subtitle = path,
                    isDirectory = isDirectory,
                    isFavorite = false,
                    iconRes = iconResFor(isDirectory)
                )
            )
        }
        return result
    }

    private fun serialize(entries: List<ShortcutEntryUi>): String {
        val array = JSONArray()
        for (entry in entries) {
            array.put(
                JSONObject()
                    .put("path", entry.path)
                    .put("name", entry.name)
                    .put("isDirectory", entry.isDirectory)
            )
        }
        return array.toString()
    }
}

private fun FileItem.toRecentEntryUi(): ShortcutEntryUi {
    val pathString = path.toString()
    val isDirectory = attributes.isDirectory
    return ShortcutEntryUi(
        path = pathString,
        name = name,
        subtitle = pathString,
        isDirectory = isDirectory,
        isFavorite = false,
        iconRes = iconResFor(isDirectory)
    )
}
