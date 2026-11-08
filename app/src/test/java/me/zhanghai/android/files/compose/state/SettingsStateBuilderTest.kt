/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The settings builder must always return the static sections (action rows do not depend on
 * [me.zhanghai.android.files.settings.Settings]) and the query filter must narrow them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsStateBuilderTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun build_withoutQuery_returnsAllSectionsWithTitledItems() {
        val state = SettingsStateBuilder.build(context)

        assertEquals(listOf("appearance", "behavior", "advanced"), state.sections.map { it.id })
        assertTrue(state.sections.flatMap { it.items }.isNotEmpty())
        state.sections.flatMap { it.items }.forEach { item ->
            assertTrue(item.title.isNotBlank())
        }
    }

    @Test
    fun build_withUnmatchedQuery_returnsNoSections() {
        val state = SettingsStateBuilder.build(context, "xyznonexistentquery")

        assertTrue(state.sections.isEmpty())
        assertEquals("xyznonexistentquery", state.query)
    }

    @Test
    fun build_withMatchingQuery_keepsOnlyMatchingItems() {
        val state = SettingsStateBuilder.build(context, "hidden")

        assertTrue(state.sections.isNotEmpty())
        state.sections.flatMap { it.items }.forEach { item ->
            val haystack = listOfNotNull(item.title, item.summary, item.valueLabel)
                .joinToString(" ")
            assertTrue(haystack.contains("hidden", ignoreCase = true))
        }
    }

    @Test
    fun build_coversEveryLegacySettingsRow() {
        val state = SettingsStateBuilder.build(context)
        val keys = state.sections.flatMap { it.items }.map { it.key }.toSet()

        assertTrue(
            keys.containsAll(
                setOf(
                    "appearance_locale",
                    "appearance_night_mode",
                    "appearance_black_theme",
                    "appearance_animations",
                    "appearance_dynamic_color",
                    "appearance_ellipsize",
                    "behavior_show_hidden",
                    "behavior_default_folder",
                    "behavior_storages",
                    "behavior_standard_dirs",
                    "behavior_bookmarks",
                    "behavior_root_strategy",
                    "behavior_archive_encoding",
                    "behavior_open_apk",
                    "behavior_remote_thumbnail",
                    "advanced_reset"
                )
            )
        )
    }

    @Test
    fun choiceRows_exposeOptions() {
        // Option structure only: the Settings backend cannot initialize on the JVM
        // (it needs the device filesystem provider), so selection state is covered
        // on-device instead.
        val state = SettingsStateBuilder.build(context)
        val items = state.sections.flatMap { it.items }.associateBy { it.key }

        val night = items.getValue("appearance_night_mode")
        assertEquals(5, night.options.size)
        assertEquals(setOf("0", "1", "2", "3", "4"), night.options.map { it.value }.toSet())

        val root = items.getValue("behavior_root_strategy")
        assertEquals(3, root.options.size)
        assertEquals(setOf("0", "1", "2"), root.options.map { it.value }.toSet())

        val encoding = items.getValue("behavior_archive_encoding")
        assertTrue(encoding.options.isNotEmpty())
        assertTrue(encoding.options.any { it.value.equals("UTF-8", ignoreCase = true) })
    }

    @Test
    fun build_doesNotContainRemovedRows() {
        val state = SettingsStateBuilder.build(context)
        val keys = state.sections.flatMap { it.items }.map { it.key }.toSet()

        assertTrue(keys.intersect(setOf("appearance_theme_color", "appearance_material3")).isEmpty())
    }

    @Test
    fun query_removedRow_returnsNoSections() {
        val state = SettingsStateBuilder.build(context, "theme color")

        assertTrue(state.sections.isEmpty())
    }

    @Test
    fun build_everyItemHasAnIcon() {
        val state = SettingsStateBuilder.build(context)
        val items = state.sections.flatMap { it.items }

        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            assertTrue(item.key, item.icon != null)
        }
    }
}
