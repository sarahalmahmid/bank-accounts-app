package com.example.bankofsarah

import android.app.Application

class BankOfSarahApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}