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
}
