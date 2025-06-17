package com.makinul.instragram.video.downloader.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostApiError(
    val error: String?
)
