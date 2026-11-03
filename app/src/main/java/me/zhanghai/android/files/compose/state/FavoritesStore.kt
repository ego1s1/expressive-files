/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import android.content.SharedPreferences
import java8.nio.file.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.compose.model.ShortcutEntryUi
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeTypeIcon
import me.zhanghai.android.files.filelist.name
import org.json.JSONArray
import org.json.JSONObject

/**
 * Process-wide persistence for user favourites.
 *
 * This deliberately exposes an object instead of requiring a ViewModel so both the Compose shell
 * and the legacy drawer can observe the same [StateFlow]. The `invoke` operator is a convenience
 * for call sites that prefer `FavoritesStore(context)`.
 */
object FavoritesStore {
    private const val PREFERENCES_NAME = "expressive_favorites"
    private const val KEY_ENTRIES = "entries"
    private const val MAX_ENTRIES = 200

    private val lock = Any()

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var isLoaded = false

    private val _favorites = MutableStateFlow<List<ShortcutEntryUi>>(emptyList())
    private val favoritesState = _favorites.asStateFlow()

    val favorites: StateFlow<List<ShortcutEntryUi>>
        get() {
            ensureLoaded()
            return favoritesState
        }

    operator fun invoke(context: Context): FavoritesStore {
        initialize(context)
        return this
    }

    /** Binds this store to the process application context and loads persisted entries once. */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        ensureLoaded()
    }

    fun isFavorite(path: Path): Boolean {
        ensureLoaded()
        val pathString = path.toString()
        return _favorites.value.any { it.path == pathString }
    }

    /**
     * Adds [item] to favourites, or removes it if it was already present.
     *
     * @return `true` when the item is a favourite after this call.
     */
    fun toggle(item: FileItem): Boolean = synchronized(lock) {
        ensureLoaded()
        val itemPath = item.path.toString()
        val current = _favorites.value
        val existing = current.any { it.path == itemPath }
        val updated = if (existing) {
            current.filterNot { it.path == itemPath }
        } else {
            (listOf(item.toShortcutEntryUi()) + current).take(MAX_ENTRIES)
        }
        _favorites.value = updated
        persist(updated)
        !existing
    }

    fun remove(path: Path) {
        synchronized(lock) {
            ensureLoaded()
            val pathString = path.toString()
            val updated = _favorites.value.filterNot { it.path == pathString }
            if (updated.size != _favorites.value.size) {
                _favorites.value = updated
                persist(updated)
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
                _favorites.value = loaded
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
                    isFavorite = true,
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

private fun FileItem.toShortcutEntryUi(): ShortcutEntryUi {
    val pathString = path.toString()
    val isDirectory = attributes.isDirectory
    return ShortcutEntryUi(
        path = pathString,
        name = name,
        subtitle = pathString,
        isDirectory = isDirectory,
        isFavorite = true,
        iconRes = iconResFor(isDirectory)
    )
}

internal fun iconResFor(isDirectory: Boolean): Int = if (isDirectory) {
    MimeTypeIcon.DIRECTORY.resourceId
} else {
    MimeTypeIcon.GENERIC.resourceId
}
