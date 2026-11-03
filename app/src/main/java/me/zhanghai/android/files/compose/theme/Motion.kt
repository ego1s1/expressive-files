/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Spring specs for custom expressive animations.
 *
 * Material 3 1.4.0 keeps `MotionScheme.expressive()` internal, so these helpers are the public
 * fallback for one-off animations. Built-in components still use Material's default motion.
 */
object ExpressiveMotion {
    /** Spatial motion for size, position, and shape changes. */
    fun <T> spatial(): SpringSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 700f
    )

    /** Effects motion for color and alpha changes. */
    fun <T> effects(): SpringSpec<T> = spring(
        dampingRatio = 1f,
        stiffness = 1600f
    )
}
