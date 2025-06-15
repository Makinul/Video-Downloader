package com.makinul.instragram.video.downloader.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val caption: String?,
    val thumbnail_url: String?,
    val video_url: String?
)