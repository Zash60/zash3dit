package com.zash3dit.domain.model

data class AudioClip(
    val id: Long = 0,
    val projectId: Long,
    val filePath: String,
    val startTime: Long, // in milliseconds
    val duration: Long,
    val position: Int,
    val volume: Float = 1.0f
)