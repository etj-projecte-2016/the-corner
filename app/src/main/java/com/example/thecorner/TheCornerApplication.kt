package com.example.thecorner

import android.app.Application
import com.example.thecorner.di.AppContainer

class TheCornerApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
    }
}