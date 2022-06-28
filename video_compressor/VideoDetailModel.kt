package com.example.database_part_3.video_compressor

import android.net.Uri

data class VideoDetailsModel(
    val playableVideoPath: String?,
    val uri: Uri,
    val newSize: String,
    val progress: Float = 0F
)