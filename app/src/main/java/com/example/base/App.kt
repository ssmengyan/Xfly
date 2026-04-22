package com.hook.fakewifi

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        lateinit var app: Context
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}
