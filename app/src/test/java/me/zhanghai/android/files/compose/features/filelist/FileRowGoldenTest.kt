/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.filelist

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot goldens for the file row.
 *
 * The comparison uses a 32x32 downscaled signature rather than an exact pixel match so
 * font antialiasing differences between macOS and Linux CI do not make the test flaky,
 * while layout, theme and selection changes still move the signature.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FileRowGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unselectedRow_matchesGolden() {
        assertGolden(selected = false, resource = "goldens/file_row_unselected.png")
    }

    @Test
    fun selectedRow_matchesGolden() {
        assertGolden(selected = true, resource = "goldens/file_row_selected.png")
    }

    private fun assertGolden(selected: Boolean, resource: String) {
        composeRule.setContent {
            ExpressiveFilesTheme(dynamicColor = false) {
                FileRow(
                    entry = FileEntryUi(
                        path = "/tmp/photo.jpg",
                        name = "photo.jpg",
                        supportingText = "1.2 MB · Oct 26",
                        isSelected = selected
                    ),
                    onClick = {},
                    onLongClick = {},
                    onMenuClick = {}
                )
            }
        }
        val actual = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val golden = javaClass.classLoader!!.getResourceAsStream(resource)!!.use {
            BitmapFactory.decodeStream(it)
        }
        val delta = signatureDelta(actual, golden)
        assertTrue("$resource differs by $delta (limit 12)", delta < 12.0)
    }

    private fun signatureDelta(actual: Bitmap, golden: Bitmap): Double {
        val size = 32
        val a = Bitmap.createScaledBitmap(actual, size, size, true)
        val g = Bitmap.createScaledBitmap(golden, size, size, true)
        var total = 0L
        for (y in 0 until size) {
            for (x in 0 until size) {
                val pa = a.getPixel(x, y)
                val pg = g.getPixel(x, y)
                total += kotlin.math.abs(Color.red(pa) - Color.red(pg)).toLong()
                total += kotlin.math.abs(Color.green(pa) - Color.green(pg)).toLong()
                total += kotlin.math.abs(Color.blue(pa) - Color.blue(pg)).toLong()
            }
        }
        return total.toDouble() / (size * size * 3)
    }
}
