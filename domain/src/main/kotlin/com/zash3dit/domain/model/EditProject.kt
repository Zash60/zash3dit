package com.zash3dit.domain.model

enum class ExportResolution(val width: Int, val height: Int, val displayName: String) {
    HD_720("1280x720", 1280, 720, "HD (1280x720)"),
    FULL_HD_1080("1920x1080", 1920, 1080, "Full HD (1920x1080)");

    override fun toString() = displayName
}

enum class ExportQuality(val bitrate: String, val displayName: String) {
    LOW("1000k", "Low (1000k)"),
    MEDIUM("2500k", "Medium (2500k)"),
    HIGH("5000k", "High (5000k)"),
    ULTRA("8000k", "Ultra (8000k)");

    override fun toString() = displayName
}

data class EditProject(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val resolution: String, // e.g., "1920x1080"
    val frameRate: Int,
    val videoClips: List<VideoClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList()
)