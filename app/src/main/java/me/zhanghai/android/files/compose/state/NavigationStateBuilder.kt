/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import android.content.Context
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.model.DrawerEntryUi
import me.zhanghai.android.files.compose.model.DrawerSectionUi
import me.zhanghai.android.files.compose.model.NavigationUiState
import me.zhanghai.android.files.compose.model.ShortcutEntryUi
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

/**
 * Builds the navigation drawer model from the same settings the legacy drawer uses.
 *
 * Everything read from [Settings] is best-effort: settings LiveData may not be initialised in
 * tests or during process startup, and one bad storage entry must not take down the drawer.
 */
object NavigationStateBuilder {
    fun build(context: Context, currentPath: Path? = null): NavigationUiState {
        FavoritesStore.initialize(context)
        RecentsStore.initialize(context)
        val currentPathString = currentPath?.toString()

        val favorites = FavoritesStore.favorites.value.map {
            it.toDrawerEntryUi(currentPathString)
        }
        val recents = RecentsStore.recents.value.map {
            it.toDrawerEntryUi(currentPathString)
        }
        val storages = loadStorages(context, currentPathString)
        val bookmarks = loadBookmarks(context, currentPathString)

        val sections = buildList {
            if (favorites.isNotEmpty()) {
                add(
                    DrawerSectionUi(
                        id = "favorites",
                        title = "Favorites", // TODO: localize
                        entries = favorites
                    )
                )
            }
            if (recents.isNotEmpty()) {
                add(
                    DrawerSectionUi(
                        id = "recents",
                        title = "Recents", // TODO: localize
                        entries = recents
                    )
                )
            }
            if (storages.isNotEmpty()) {
                add(
                    DrawerSectionUi(
                        id = "storages",
                        title = "Storages", // TODO: localize
                        entries = storages
                    )
                )
            }
            if (bookmarks.isNotEmpty()) {
                add(
                    DrawerSectionUi(
                        id = "bookmarks",
                        title = "Bookmarks", // TODO: localize
                        entries = bookmarks
                    )
                )
            }
            add(
                DrawerSectionUi(
                    id = "more",
                    title = "More", // TODO: localize
                    entries = listOf(
                        DrawerEntryUi(
                            id = ACTION_SETTINGS,
                            title = context.getString(R.string.navigation_settings),
                            iconRes = R.drawable.settings_icon_white_24dp,
                            isAction = true
                        ),
                        DrawerEntryUi(
                            id = ACTION_ABOUT,
                            title = context.getString(R.string.navigation_about),
                            iconRes = R.drawable.information_icon_white_24dp,
                            isAction = true
                        )
                    )
                )
            )
        }

        return NavigationUiState(
            appName = context.getString(R.string.app_name),
            version = runCatching { context.getString(R.string.app_version) }.getOrDefault(""),
            sections = sections
        )
    }

    private fun loadStorages(context: Context, currentPathString: String?): List<DrawerEntryUi> =
        runCatching { Settings.STORAGES.valueCompat }
            .getOrNull()
            .orEmpty()
            .filter { runCatching { it.isVisible }.getOrDefault(false) }
            .mapNotNull { storage ->
                runCatching {
                    val path = storage.path
                    val pathString = path?.toString()
                    DrawerEntryUi(
                        id = pathString ?: "storage:${storage.id}",
                        title = storage.getName(context),
                        subtitle = storage.description.takeIf { it.isNotBlank() },
                        iconRes = storage.iconRes,
                        selected = pathString != null && pathString == currentPathString
                    )
                }.getOrNull()
            }

    private fun loadBookmarks(context: Context, currentPathString: String?): List<DrawerEntryUi> =
        runCatching { Settings.BOOKMARK_DIRECTORIES.valueCompat }
            .getOrNull()
            .orEmpty()
            .mapNotNull { bookmark ->
                runCatching {
                    val pathString = bookmark.path.toString()
                    DrawerEntryUi(
                        id = pathString,
                        title = bookmark.name,
                        subtitle = pathString,
                        iconRes = R.drawable.directory_icon_white_24dp,
                        selected = pathString == currentPathString
                    )
                }.getOrNull()
            }

    const val ACTION_SETTINGS = "action_settings"
    const val ACTION_ABOUT = "action_about"
}

private fun ShortcutEntryUi.toDrawerEntryUi(currentPathString: String?): DrawerEntryUi =
    DrawerEntryUi(
        id = path,
        title = name,
        subtitle = subtitle,
        iconRes = iconRes,
        selected = path == currentPathString
    )

/** Convenience alias for call sites that use function naming rather than the object. */
fun buildNavigationUiState(
    context: Context,
    currentPath: Path? = null
): NavigationUiState = NavigationStateBuilder.build(context, currentPath)
