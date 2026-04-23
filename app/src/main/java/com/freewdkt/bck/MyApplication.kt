package com.freewdkt.bck

import android.app.Application
import com.freewdkt.bck.utils.SessionManager

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
    }
}