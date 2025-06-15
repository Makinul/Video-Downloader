package com.makinul.instragram.video.downloader.data.repository

import com.makinul.instragram.video.downloader.data.model.Post
import com.makinul.instragram.video.downloader.data.service.ApiService
import java.io.File

// In MyRepository (updated)
class MyRepository(
    private val apiService: ApiService // Inject your Ktor API service
) {
    suspend fun fetchInstaVideo(instaReelUrl: String): Post {
        return apiService.fetchInstaVideo(instaReelUrl)
    }

    suspend fun downloadInstaVideo(
        instaReelUrl: String,
        onProgress: (Float) -> Unit,
        onComplete: (File) -> Unit
    ) {
        return apiService.downloadInstaVideo(
            instaReelUrl, onProgress, onComplete
        )
    }
}