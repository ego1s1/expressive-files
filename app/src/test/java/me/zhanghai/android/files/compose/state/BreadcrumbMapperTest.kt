/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.state

import androidx.test.core.app.ApplicationProvider
import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.filelist.BreadcrumbData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Forward trail history left behind by `navigateUp` must never be presented as the
 * current path: only the trail up to and including the selected folder is mapped.
 *
 * Uses a fake [Path] because the `java8.nio` backport requires an installed filesystem
 * provider, which is only available on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BreadcrumbMapperTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private val root = FakePath("/storage/emulated/0")
    private val android = FakePath("/storage/emulated/0/Android")
    private val data = FakePath("/storage/emulated/0/Android/data")

    private fun data(selectedIndex: Int) = BreadcrumbData(
        paths = listOf(root, android, data),
        nameProducers = listOf(
            { _ -> "Internal storage" },
            { _ -> "Android" },
            { _ -> "data" }
        ),
        selectedIndex = selectedIndex
    )

    @Test
    fun fullSelection_mapsWholeTrailWithCurrentLast() {
        val crumbs = data(selectedIndex = 2).toBreadcrumbUi(context, data)

        assertEquals(listOf("Internal storage", "Android", "data"), crumbs.map { it.label })
        assertEquals(listOf(false, false, true), crumbs.map { it.isCurrent })
    }

    @Test
    fun afterNavigateUpToRoot_forwardHistoryIsDropped() {
        val crumbs = data(selectedIndex = 0).toBreadcrumbUi(context, root)

        assertEquals(listOf("Internal storage"), crumbs.map { it.label })
        assertTrue(crumbs.single().isCurrent)
    }

    @Test
    fun afterOneNavigateUp_trailEndsAtCurrent() {
        val crumbs = data(selectedIndex = 1).toBreadcrumbUi(context, android)

        assertEquals(listOf("Internal storage", "Android"), crumbs.map { it.label })
        assertEquals(listOf(false, true), crumbs.map { it.isCurrent })
    }

    @Test
    fun outOfRangeSelection_fallsBackToCurrentPath() {
        val crumbs = data(selectedIndex = 99).toBreadcrumbUi(context, android)

        assertEquals(listOf("Internal storage", "Android"), crumbs.map { it.label })
        assertEquals(listOf(false, true), crumbs.map { it.isCurrent })
    }

    @Test
    fun emptyTrail_mapsToNothing() {
        val crumbs = BreadcrumbData(emptyList(), emptyList(), 0)
            .toBreadcrumbUi(context, root)

        assertTrue(crumbs.isEmpty())
    }

    private class FakePath(private val value: String) : Path {
        override fun getFileSystem(): java8.nio.file.FileSystem =
            throw UnsupportedOperationException()

        override fun isAbsolute(): Boolean = value.startsWith("/")

        override fun getRoot(): Path? = if (isAbsolute()) FakePath("/") else null

        override fun getFileName(): Path? =
            value.substringAfterLast('/').takeIf { it.isNotEmpty() }?.let(::FakePath)

        override fun getParent(): Path? =
            value.substringBeforeLast('/', "").takeIf { it.isNotEmpty() }?.let(::FakePath)

        override fun getNameCount(): Int = 0

        override fun getName(index: Int): Path = throw UnsupportedOperationException()

        override fun subpath(beginIndex: Int, endIndex: Int): Path =
            throw UnsupportedOperationException()

        override fun startsWith(other: Path): Boolean = false

        override fun startsWith(other: String): Boolean = value.startsWith(other)

        override fun endsWith(other: Path): Boolean = false

        override fun endsWith(other: String): Boolean = value.endsWith(other)

        override fun normalize(): Path = this

        override fun resolve(other: Path): Path = throw UnsupportedOperationException()

        override fun resolve(other: String): Path = throw UnsupportedOperationException()

        override fun resolveSibling(other: Path): Path = throw UnsupportedOperationException()

        override fun resolveSibling(other: String): Path = throw UnsupportedOperationException()

        override fun relativize(other: Path): Path = throw UnsupportedOperationException()

        override fun toUri(): java.net.URI = throw UnsupportedOperationException()

        override fun toAbsolutePath(): Path = this

        override fun toRealPath(vararg options: java8.nio.file.LinkOption): Path = this

        override fun toFile(): java.io.File = java.io.File(value)

        override fun register(
            watcher: WatchService,
            vararg events: WatchEvent.Kind<*>
        ): WatchKey = throw UnsupportedOperationException()

        override fun register(
            watcher: WatchService,
            events: Array<out WatchEvent.Kind<*>>,
            vararg modifiers: WatchEvent.Modifier
        ): WatchKey = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<Path> = mutableListOf<Path>().iterator()

        override fun compareTo(other: Path): Int = value.compareTo(other.toString())

        override fun equals(other: Any?): Boolean = other is FakePath && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value
    }
}
