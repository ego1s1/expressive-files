/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Spring specs for custom expressive animations.
 *
 * Material3 1.5 exposes [androidx.compose.material3.MotionScheme.expressive] through
 * [androidx.compose.material3.MaterialExpressiveTheme]; built-in components read it automatically.
 * These helpers remain for custom motion that is not component-owned.
 */
object ExpressiveMotion {
    /** Spatial motion for size, position, and shape changes. */
    fun <T> spatial(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 380f
    )

    /** Fast spatial motion for small, immediate state changes. */
    fun <T> spatialFast(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 800f
    )

    /** Slow spatial motion for large surfaces such as sheets. */
    fun <T> spatialSlow(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = 200f
    )

    /** Effects motion for color and alpha changes. */
    fun <T> effects(): SpringSpec<T> = spring(
        dampingRatio = 1f,
        stiffness = 1600f
    )

    /**
     * Settle motion for gesture-driven surfaces (predictive back commit/cancel).
     * Critically damped: lands exactly, no overshoot bounce on full-window content.
     */
    fun <T> settle(): SpringSpec<T> = spring(
        dampingRatio = 1f,
        stiffness = 1000f
    )
}

/**
 * Material 3 expressive easing curves, using the official cubic-bezier values.
 *
 * These are defined locally (rather than depending on alpha-only easing APIs) so custom
 * transitions stay stable across backend upgrades.
 */
val ExpressiveDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val ExpressiveAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
val ExpressiveStandardEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Whether the user allows animations.
 *
 * Compose components and custom transitions must land in their final state when the system
 * animator duration scale is zero. This reads the platform setting once per composition; the
 * value is stable for the lifetime of the screen, which is sufficient for a transition decision.
 */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    val appMotionEnabled = LocalAppMotionEnabled.current
    return remember(context, appMotionEnabled) {
        appMotionEnabled && Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}
