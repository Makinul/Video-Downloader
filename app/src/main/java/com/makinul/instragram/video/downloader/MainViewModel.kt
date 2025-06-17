package com.makinul.instragram.video.downloader

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinul.instragram.video.downloader.data.model.Post
import com.makinul.instragram.video.downloader.data.model.PostApiError
import com.makinul.instragram.video.downloader.data.repository.MyRepository
import com.makinul.instragram.video.downloader.di.httpClient
import com.makinul.instragram.video.downloader.utils.AppConstant.showLog
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.net.ConnectException

data class MyUiState(
    val message: String = "",
    val isLoading: Boolean = false,
    val progress: Float = -1f,
    val posts: Post? = null,
    val error: String? = null
)

class MainViewModel(
    private val myRepository: MyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState

    fun fetchInstaVideo(context: Context, instaUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val post =
                    myRepository.fetchInstaVideo(instaUrl)

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    progress = 0.01f,
                    posts = post
                )

                showLog()

                if (post.video_url.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load instagram video"
                    )
                    return@launch
                }

                downloadInstaVideo(context = context, videoUrl = post.video_url)
            } catch (e: ClientRequestException) {
                // 4xx responses
                val statusCode = e.response.status.value
                val errorBody = e.response.bodyAsText()
                val apiError = try {
                    Json.decodeFromString<PostApiError>(errorBody)
                } catch (parseError: Exception) {
                    null // fallback if parsing fails
                }
                val error = apiError?.error ?: "Client side error"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = -1f,
                    message = "",
                    posts = null,
                    error = error
                )
            } catch (e: ServerResponseException) {
                // 5xx responses
                val statusCode = e.response.status.value
                val errorBody = e.response.bodyAsText()
                val apiError = try {
                    Json.decodeFromString<PostApiError>(errorBody)
                } catch (parseError: Exception) {
                    null // fallback if parsing fails
                }
                val error = apiError?.error ?: "Server side error"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = -1f,
                    message = "",
                    posts = null,
                    error = error
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = -1f,
                    message = "",
                    posts = null,
                    error = "Unexpected error: ${e.localizedMessage}"
                )
            }
        }
    }

    // Add more functions here to handle UI events and update state
    fun resetUiState() {
        _uiState.value = MyUiState()
    }

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    fun updateProgress(value: Float) {
        _downloadProgress.value = value.coerceIn(0f, 1f)
    }

    private suspend fun downloadInstaVideo(context: Context, videoUrl: String) {
        try {
            // Use the singleton Ktor client to make the request
            val response = httpClient.get(videoUrl)

            // Get the body as a ByteReadChannel
            val channel: ByteReadChannel = response.body()
            val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
            // Convert the channel to an InputStream and save it
            showLog("contentLength $contentLength")
            val savedUri = saveVideoToGallery(
                context = context,
                inputStream = channel.toInputStream(), // <-- Ktor provides this extension
                fileName = "instagram_video_${System.currentTimeMillis()}.mp4",
                totalSizeInBytes = contentLength,
                onProgress = { progress ->
                    updateProgress(progress)
                }
            )

            if (savedUri != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = 1f,
                    message = "Download complete",
                    error = null,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = -1f,
                    message = "",
                    posts = null,
                    error = "Download failed"
                )
            }
        } catch (e: Exception) {
            // Handle Ktor network exceptions (e.g., HttpRequestTimeoutException, ConnectException)
            e.printStackTrace()
            when (e) {
                is HttpRequestTimeoutException -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progress = -1f,
                        message = "",
                        posts = null,
                        error = "Server not responding"
                    )
                }

                is ConnectException -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progress = -1f,
                        message = "",
                        posts = null,
                        error = "Network connection error, please check and retry"
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progress = -1f,
                        message = "",
                        posts = null,
                        error = "Unknown error occurred, please try again"
                    )
                }
            }
        }
    }

    private suspend fun saveVideoToGallery(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        totalSizeInBytes: Long,
        onProgress: (Float) -> Unit
    ): String? {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    // Save the video in the Movies directory
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    // Mark the file as pending while it's being written
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                // Insert a new entry into the MediaStore. This returns a URI for the new file.
                val collection =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val videoUri = resolver.insert(collection, contentValues)

                videoUri?.let { uri ->
                    try {
                        // Open an output stream to the new URI
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var totalBytesRead = 0L

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                if (totalSizeInBytes > 0) {
                                    val progress = totalBytesRead.toFloat() / totalSizeInBytes
                                    onProgress(progress.coerceIn(0f, 1f))
                                }
                            }
                        }

                        // Now that the file is fully written, update the IS_PENDING flag to 0
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)

                        return@withContext uri.toString()
                    } catch (e: Exception) {
                        // If something goes wrong, delete the incomplete entry
                        resolver.delete(uri, null, null)
                        e.printStackTrace()
                    }
                }
            } else {
                // Fallback for older Android versions (see below)
                // This requires WRITE_EXTERNAL_STORAGE permission
            }
            return@withContext null
        }
    }
}