package com.zash3dit.domain.usecase

import com.zash3dit.domain.repository.EditProjectRepository

class ApplyTextOverlayUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        clipId: Long,
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")
        val clip = project.videoClips.find { it.id == clipId } ?: throw IllegalArgumentException("Clip not found")

        val inputPath = clip.filePath

        // Get text overlays that overlap with this clip's time range
        val relevantOverlays = project.textOverlays.filter { overlay ->
            // Check if overlay time range overlaps with clip time range
            val overlayEndTime = overlay.startTime + overlay.duration
            val clipEndTime = clip.startTime + clip.duration
            overlay.startTime < clipEndTime && overlayEndTime > clip.startTime
        }

        if (relevantOverlays.isEmpty()) {
            // No overlays to apply, just copy the video
            return "-i \"$inputPath\" -c copy \"$outputPath\""
        }

        // Build drawtext filters for each overlay
        val drawtextFilters = relevantOverlays.map { overlay ->
            val relativeStartTime = maxOf(0L, overlay.startTime - clip.startTime) / 1000.0
            val relativeEndTime = minOf(clip.duration, overlay.startTime + overlay.duration - clip.startTime) / 1000.0

            // Convert hex color to FFmpeg format (remove # if present)
            val color = overlay.color.removePrefix("#")

            // Position as percentage or pixels
            val xPos = if (overlay.x <= 1.0f) "${(overlay.x * 100).toInt()}%" else "${overlay.x.toInt()}"
            val yPos = if (overlay.y <= 1.0f) "${(overlay.y * 100).toInt()}%" else "${overlay.y.toInt()}"

            "drawtext=text='${overlay.text}':fontcolor=0x$color:fontsize=${overlay.fontSize}:" +
            "x=$xPos:y=$yPos:enable='between(t,$relativeStartTime,$relativeEndTime)'"
        }

        val vf = if (drawtextFilters.isNotEmpty()) "-vf \"${drawtextFilters.joinToString(",")}\"" else ""

        val command = "-i \"$inputPath\" $vf -c:v libx264 -c:a aac \"$outputPath\""

        return command.trim()
    }
}