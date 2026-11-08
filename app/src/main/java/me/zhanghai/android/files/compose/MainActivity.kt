/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.compose.theme.ExpressiveFilesTheme
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

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
        val initialRoute = intent.getStringExtra(EXTRA_ROUTE)
        setContent {
            val blackTheme by Settings.BLACK_NIGHT_MODE.observeAsState(
                Settings.BLACK_NIGHT_MODE.valueCompat
            )
            val dynamicColor by Settings.DYNAMIC_COLOR.observeAsState(
                Settings.DYNAMIC_COLOR.valueCompat
            )
            val animationsEnabled by Settings.FILE_LIST_ANIMATION.observeAsState(
                Settings.FILE_LIST_ANIMATION.valueCompat
            )
            ExpressiveFilesTheme(
                blackTheme = blackTheme,
                dynamicColor = dynamicColor,
                animationsEnabled = animationsEnabled
            ) {
                ExpressiveFilesApp(initialRoute = initialRoute)
            }
        }
    }

    companion object {
        const val EXTRA_ROUTE = "extra_route"

        fun createSettingsIntent(context: Context, route: String = "settings"): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_ROUTE, route)
    }
}
