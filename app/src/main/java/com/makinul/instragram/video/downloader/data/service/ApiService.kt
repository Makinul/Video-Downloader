package com.makinul.instragram.video.downloader.data.service

import android.os.Environment
import com.makinul.instragram.video.downloader.BuildConfig
import com.makinul.instragram.video.downloader.data.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.FileOutputStream

interface ApiService {
    suspend fun fetchInstaVideo(instaReelUrl: String): Post
    suspend fun downloadInstaVideo(
        instaReelUrl: String,
        onProgress: (Float) -> Unit,
        onComplete: (File) -> Unit
    )
}

class ApiServiceImpl(private val httpClient: HttpClient) : ApiService {

    override suspend fun fetchInstaVideo(instaReelUrl: String): Post {
        return httpClient.get("${BuildConfig.BASE_URL}get-instagram-video?url=$instaReelUrl")
            .body()
    }

    override suspend fun downloadInstaVideo(
        instaReelUrl: String,
        onProgress: (Float) -> Unit,
        onComplete: (File) -> Unit
    ) {
        val fileName = "instagram_video_${System.currentTimeMillis()}.mp4"
        val videoDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val videoFile = File(videoDir, fileName)

        val response = httpClient.get(instaReelUrl)
        val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L

        val input = response.bodyAsChannel()
        val output = FileOutputStream(videoFile)

        var bytesCopied = 0L
        val buffer = ByteArray(8 * 1024)

        while (!input.isClosedForRead) {
            val read = input.readAvailable(buffer, 0, buffer.size)
            if (read == -1) break
            output.write(buffer, 0, read)
            bytesCopied += read

            if (contentLength > 0) {
                val progress = bytesCopied.toFloat() / contentLength
                onProgress(progress)
            }
        }

        output.flush()
        output.close()
        input.cancel()

        onComplete(videoFile)
        httpClient.close()
    }
}