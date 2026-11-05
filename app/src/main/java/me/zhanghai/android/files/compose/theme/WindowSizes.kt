/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.theme

import androidx.compose.ui.unit.dp

/**
 * Shared window-size breakpoints for the Compose shell.
 *
 * The app intentionally uses a small, explicit set instead of pulling in the adaptive
 * library: the navigation rail and label thresholds are the only decisions the shell
 * makes today. Keep them here so a screen cannot invent a fourth breakpoint.
 */
object WindowSizes {
    /** Below this width the floating bottom pill is used. */
    val MediumMinWidth = 600.dp

    /** At and above this width the list should gain an expanded two-pane treatment. */
    val ExpandedMinWidth = 840.dp

    /** Below this width the selected pill destination stays icon-only. */
    val PillLabelMinWidth = 390.dp
}
