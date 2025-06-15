package com.makinul.instragram.video.downloader.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.makinul.instragram.video.downloader.MainViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val context = LocalContext.current

    /* Content for Download screen */
    Column(
        modifier = modifier
            .fillMaxSize() // Make the box fill the available space
            .background(MaterialTheme.colorScheme.surfaceVariant) // Set background color
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
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
            onClick = { viewModel.downloadInstaVideo(context = context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Video", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        HowToUseCard()
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