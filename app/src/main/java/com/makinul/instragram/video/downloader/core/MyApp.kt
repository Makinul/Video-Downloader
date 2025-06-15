package com.makinul.instragram.video.downloader.core

import android.app.Application
import com.makinul.instragram.video.downloader.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule) // Pass your Koin modules here
        }
    }
}