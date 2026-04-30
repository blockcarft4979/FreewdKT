package com.freewdkt.bck

import android.app.Application
import com.freewdkt.bck.utils.SessionManager
import android.os.Build
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
        lateinit var sessionManager: SessionManager
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}