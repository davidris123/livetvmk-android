package com.example.tvappmk.data

import android.app.Application

class DatabaseSetup : Application() {

    companion object {
        lateinit var channelDatabase: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        channelDatabase = AppDatabase.getDatabase(applicationContext)
    }
}