/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.os.Bundle
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.BundleParceler
import me.zhanghai.android.files.util.ParcelableArgs

class SettingsActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The legacy preference UI is retired: deep links (including the system
        // APPLICATION_PREFERENCES action) land on the unified Compose settings screen.
        startActivity(
            me.zhanghai.android.files.compose.MainActivity.createSettingsIntent(this)
        )
        finish()
    }

    @Parcelize
    class Args(val savedInstanceState: @WriteWith<BundleParceler> Bundle?) : ParcelableArgs
}
