package com.android.aschat.feature_host.presentation

import androidx.navigation.NavController
import com.android.aschat.feature_home.domain.model.wall.subtag.HostData
import com.android.aschat.feature_home.presentation.HomeEvents

sealed class HostEvents {
    // 主播资料页

    class SendHostData(val hostData: HostData): HostEvents()
    class ExitHostDetail(val activity: HostActivity): HostEvents()
    class JumpFullScreenImage(val navController: NavController, val position: Int): HostEvents()
    class JumpFullScreenVideo(val navController: NavController, val position: Int): HostEvents()
    class ExitFullScreenImage(val navController: NavController): HostEvents()
    class ExitFullScreenVideo(val navController: NavController): HostEvents()
}