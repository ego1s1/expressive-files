/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme

/**
 * Compose-first entry point for Expressive Files.
 *
 * Legacy activities remain reachable for screens that have not been migrated yet
 * (settings, properties, viewers, FTP). See docs/MIGRATION.md.
 */
class MainActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpressiveFilesTheme {
                ExpressiveFilesApp()
            }
        }
    }
}
