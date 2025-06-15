package com.makinul.instragram.video.downloader.ui

import android.Manifest
import android.os.Build
import android.widget.ProgressBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.makinul.instragram.video.downloader.MainViewModel
import com.makinul.instragram.video.downloader.utils.AppConstant.isNetworkAvailable
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val permissionState = rememberPermissionState(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }
    }

    val context = LocalContext.current
    var instaUrl by remember { mutableStateOf("") }

    var isConnected by remember { mutableStateOf<Boolean?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentProgress = remember { mutableFloatStateOf(uiState.progress) }

    val errorMessage = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize() // Make the box fill the available space
            .background(MaterialTheme.colorScheme.surfaceVariant) // Set background color
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = instaUrl,
            onValueChange = { newText ->
                // 2. Update the state whenever the user types.
                instaUrl = newText
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Paste Instagram URL here...") },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isConnected = isNetworkAvailable(context)
                if (isConnected != true) {
                    errorMessage.value = "❌ No Internet"
                    return@Button
                }

                if (instaUrl.isEmpty()) {
                    errorMessage.value = "This field not be empty"
                    return@Button
                }

                if (!instaUrl.startsWith("https://www.instagram.com/")) {
                    errorMessage.value = "Please enter a valid Instagram URL"
                    return@Button
                }

                if (instaUrl.contains("?igsh=")) {
                    instaUrl = instaUrl.split("?igsh=").first()
                }

                if (instaUrl.endsWith("?utm_source=ig_web_copy_link")) {
                    instaUrl = instaUrl.replace("?utm_source=ig_web_copy_link", "")
                }
                errorMessage.value = ""
                viewModel.fetchInstaVideo(context = context, instaUrl = instaUrl)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            val buttonText = if (uiState.isLoading) {
                "Downloading Video"
            } else {
                "Download Video"
            }
            Text(buttonText, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            if (currentProgress.floatValue > 0f) {
                LinearProgressIndicator(
                    progress = { currentProgress.floatValue },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            if (uiState.progress == 1f) {
                instaUrl = ""
                errorMessage.value = ""
                Text(uiState.message, color = Color.Green)
            } else {
                if (uiState.message.isNotEmpty())
                    errorMessage.value = uiState.message
            }
        }

        if (errorMessage.value.isNotEmpty()) {
            Text(errorMessage.value, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(8.dp))

        HowToUseCard()
    }
}

@Composable
fun IndeterminateProgressExample() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(
            modifier = Modifier.width(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndeterminateProgressPreview() {
    IndeterminateProgressExample()
}

@Composable
fun DeterminateProgressExample() {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Determinate LinearProgressIndicator: ${(currentProgress * 100).toInt()}%")
        LinearProgressIndicator(
            progress = { currentProgress }, // Pass the state here
            modifier = Modifier.width(150.dp)
        )

        Spacer(Modifier.height(30.dp))

        Text("Determinate CircularProgressIndicator: ${(currentProgress * 100).toInt()}%")
        CircularProgressIndicator(
            progress = { currentProgress }, // Pass the state here
            modifier = Modifier.width(64.dp)
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = { isLoading = true },
            enabled = !isLoading
        ) {
            Text("Start Progress")
        }
    }
}

@Composable
fun HowToUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("How to use:", fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Copy the Instagram post/reel URL\n" +
                        "2. Paste it in the input field above\n" +
                        "3. Tap \"Download Video\" button\n" +
                        "4. Video will be saved to your gallery",
                lineHeight = 22.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun NetworkCheckButton() {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            isConnected = isNetworkAvailable(context)
        }) {
            Text("Check Network")
        }

        isConnected?.let { connected ->
            Text(
                text = if (connected) "✅ Internet Available" else "❌ No Internet",
                color = if (connected) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}