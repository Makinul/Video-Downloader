package com.makinul.instragram.video.downloader

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makinul.instragram.video.downloader.data.model.Post
import com.makinul.instragram.video.downloader.data.repository.MyRepository
import com.makinul.instragram.video.downloader.di.httpClient
import com.makinul.instragram.video.downloader.utils.AppConstant.showLog
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

data class MyUiState(
    val message: String = "Initial Message",
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

                if (post.video_url.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load instagram video"
                    )
                    return@launch
                }

                downloadInstaVideo(context = context, videoUrl = post.video_url)
            } catch (e: Exception) {
                print(e.message)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load posts: ${e.localizedMessage}"
                )
            }
        }
    }


    private suspend fun downloadInstaVideo(context: Context, videoUrl: String) {
//        val videoUrl =
//            "https://scontent-sof1-1.cdninstagram.com/o1/v/t16/f2/m86/AQP59AEN2R6tL5ykE7tpnINARl5jwAJhXXbRsxbypcdMg1VVKJN6lypelTeicGGdsljIe4BtSjlT5BVq2AKG17e-87UhI1rbU6Vz6ig.mp4?stp=dst-mp4&efg=eyJxZV9ncm91cHMiOiJbXCJpZ193ZWJfZGVsaXZlcnlfdnRzX290ZlwiXSIsInZlbmNvZGVfdGFnIjoidnRzX3ZvZF91cmxnZW4uY2xpcHMuYzIuNjI4LmJhc2VsaW5lIn0&_nc_cat=105&vs=1019626473639644_1371450734&_nc_vs=HBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8wMjRCNjQwMjI3Nzc2NzIyQUFBMjBBODdDOEIxQzQ4RV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HTHZRR2g1bk5NUUIta29DQU52MUZPel9GNE1yYnFfRUFBQUYVAgLIARIAKAAYABsAFQAAJpy5z6jFv9I%2FFQIoAkMzLBdAQbMzMzMzMxgSZGFzaF9iYXNlbGluZV8xX3YxEQB1%2Fgdl5p0BAA%3D%3D&ccb=9-4&oh=00_AfMgUBHcyhegJklSvIIrVPT7tqKauAEKT_7YmlV2VH6WYQ&oe=685084F3&_nc_sid=d885a2"
//            myRepository.downloadInstaVideo(
//                instaReelUrl = "https://scontent-sof1-1.cdninstagram.com/o1/v/t16/f2/m86/AQP59AEN2R6tL5ykE7tpnINARl5jwAJhXXbRsxbypcdMg1VVKJN6lypelTeicGGdsljIe4BtSjlT5BVq2AKG17e-87UhI1rbU6Vz6ig.mp4?stp=dst-mp4&efg=eyJxZV9ncm91cHMiOiJbXCJpZ193ZWJfZGVsaXZlcnlfdnRzX290ZlwiXSIsInZlbmNvZGVfdGFnIjoidnRzX3ZvZF91cmxnZW4uY2xpcHMuYzIuNjI4LmJhc2VsaW5lIn0&_nc_cat=105&vs=1019626473639644_1371450734&_nc_vs=HBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC8wMjRCNjQwMjI3Nzc2NzIyQUFBMjBBODdDOEIxQzQ4RV92aWRlb19kYXNoaW5pdC5tcDQVAALIARIAFQIYOnBhc3N0aHJvdWdoX2V2ZXJzdG9yZS9HTHZRR2g1bk5NUUIta29DQU52MUZPel9GNE1yYnFfRUFBQUYVAgLIARIAKAAYABsAFQAAJpy5z6jFv9I%2FFQIoAkMzLBdAQbMzMzMzMxgSZGFzaF9iYXNlbGluZV8xX3YxEQB1%2Fgdl5p0BAA%3D%3D&ccb=9-4&oh=00_AfMgUBHcyhegJklSvIIrVPT7tqKauAEKT_7YmlV2VH6WYQ&oe=685084F3&_nc_sid=d885a2",
//                onProgress = { progress ->
////                    _uiState.value = _uiState.value.copy(message = "Downloading... $progress%")
//                    showLog(message = "progress $progress")
//                },
//                onComplete = { file ->
////                    _uiState.value = _uiState.value.copy(message = "Download complete: ${file.path}")
//                    showLog(message = "file ${file.absolutePath}")
//                }
//            )

        try {
            // Use the singleton Ktor client to make the request
            val response = httpClient.get(videoUrl)

            // Get the body as a ByteReadChannel
            val channel: ByteReadChannel = response.body()

            // Convert the channel to an InputStream and save it
            val savedUri = saveVideoToGallery(
                context = context,
                inputStream = channel.toInputStream(), // <-- Ktor provides this extension
                fileName = "instagram_video_${System.currentTimeMillis()}.mp4"
            )

            if (savedUri != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = 1f,
                    message = "Download complete"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    progress = -1f,
                    message = "Download failed"
                )
            }
        } catch (e: Exception) {
            // Handle Ktor network exceptions (e.g., HttpRequestTimeoutException, ConnectException)
            e.printStackTrace()
        }
    }

    private suspend fun saveVideoToGallery(
        context: Context,
        inputStream: InputStream,
        fileName: String
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
                            // Copy the data from the input stream to the output stream
                            inputStream.copyTo(outputStream)
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