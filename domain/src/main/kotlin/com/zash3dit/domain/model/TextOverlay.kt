package com.zash3dit.domain.model

data class TextOverlay(
    val id: Long = 0,
    val projectId: Long,
    val text: String,
    val startTime: Long, // in milliseconds
    val duration: Long,
    val position: Int,
    val x: Float, // position on screen
    val y: Float,
    val fontSize: Int,
    val color: String // hex color
)