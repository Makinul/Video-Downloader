package com.makinul.instragram.video.downloader.di

import com.makinul.instragram.video.downloader.MainViewModel
import com.makinul.instragram.video.downloader.data.repository.MyRepository
import com.makinul.instragram.video.downloader.data.service.ApiService
import com.makinul.instragram.video.downloader.data.service.ApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val httpClient = HttpClient(Android) {
    expectSuccess = true
    install(HttpTimeout) {
        val timeout = 30000L
        connectTimeoutMillis = timeout
        requestTimeoutMillis = timeout
        socketTimeoutMillis = timeout
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.BODY
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}

val appModule = module {
    // Ktor HTTP Client as a singleton
    single { httpClient }

    // API Service as a singleton, injecting the HttpClient
    single<ApiService> { ApiServiceImpl(get()) }

    // Repository as a singleton, injecting the ApiService
    single { MyRepository(get()) }

    // ViewModel, injecting the Repository
    viewModel { MainViewModel(get()) }
}