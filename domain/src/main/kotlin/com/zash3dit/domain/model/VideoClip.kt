package com.zash3dit.domain.model

enum class VideoFilter {
    NONE,
    BLACK_AND_WHITE,
    SEPIA,
    VINTAGE
}

enum class TransitionType {
    NONE,
    FADE_IN_OUT,
    SLIDE,
    DISSOLVE
}

data class VideoClip(
    val id: Long = 0,
    val projectId: Long,
    val filePath: String,
    val startTime: Long, // in milliseconds
    val duration: Long,
    val position: Int, // order in timeline
    val trimStart: Long = 0,
    val trimEnd: Long = 0,
    val filter: VideoFilter = VideoFilter.NONE,
    val brightness: Float = 0f, // -1.0 to 1.0
    val contrast: Float = 1f, // 0.0 to 2.0
    val saturation: Float = 1f, // 0.0 to 2.0
    val playbackSpeed: Float = 1f, // 0.5 to 2.0
    val transitionType: TransitionType = TransitionType.NONE,
    val transitionDuration: Long = 1000L // in milliseconds, default 1 second
)