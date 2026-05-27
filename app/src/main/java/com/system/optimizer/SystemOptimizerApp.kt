package com.system.optimizer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SystemOptimizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
