/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the Expressive Files token contract. These are intentionally pure JVM tests:
 * a regression in the shape scale or spring specs should fail before any screenshot
 * or device test runs.
 */
class ThemeTokensTest {

    @Test
    fun shapeScale_includesExpressiveSteps() {
        assertEquals(RoundedCornerShape(4.dp), ExpressiveShapes.extraSmall)
        assertEquals(RoundedCornerShape(8.dp), ExpressiveShapes.small)
        assertEquals(RoundedCornerShape(12.dp), ExpressiveShapes.medium)
        assertEquals(RoundedCornerShape(16.dp), ExpressiveShapes.large)
        assertEquals(RoundedCornerShape(20.dp), ExpressiveShapes.largeIncreased)
        assertEquals(RoundedCornerShape(28.dp), ExpressiveShapes.extraLarge)
        assertEquals(RoundedCornerShape(32.dp), ExpressiveShapes.extraLargeIncreased)
        assertEquals(RoundedCornerShape(48.dp), ExpressiveShapes.extraExtraLarge)
    }

    @Test
    fun spatialMotion_usesExpressiveDefaults() {
        val spec = ExpressiveMotion.spatial<Float>()
        assertEquals(0.8f, spec.dampingRatio)
        assertEquals(380f, spec.stiffness)
    }

    @Test
    fun spatialMotion_hasFastAndSlowVariants() {
        assertEquals(800f, ExpressiveMotion.spatialFast<Float>().stiffness)
        assertEquals(200f, ExpressiveMotion.spatialSlow<Float>().stiffness)
    }

    @Test
    fun effectsMotion_isCriticallyDamped() {
        val spec = ExpressiveMotion.effects<Float>()
        assertEquals(1f, spec.dampingRatio)
        assertEquals(1600f, spec.stiffness)
    }

    @Test
    fun settleMotion_isCriticallyDamped() {
        // Content that must not wobble still lands exactly.
        val spec = ExpressiveMotion.settle<Float>()
        assertEquals(1f, spec.dampingRatio)
    }

    @Test
    fun bouncyMotion_overshootsWithinExpressiveRange() {
        // Expressive guidance: damping below 1 so a release feels alive.
        val spec = ExpressiveMotion.bouncy<Float>()
        assertTrue(spec.dampingRatio < 1f)
    }
}
