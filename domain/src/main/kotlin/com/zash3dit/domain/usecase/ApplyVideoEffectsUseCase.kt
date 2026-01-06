package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.VideoFilter
import com.zash3dit.domain.repository.EditProjectRepository

class ApplyVideoEffectsUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        clipId: Long,
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")
        val clip = project.videoClips.find { it.id == clipId } ?: throw IllegalArgumentException("Clip not found")

        val inputPath = clip.filePath
        val filters = mutableListOf<String>()

        // Add filter
        when (clip.filter) {
            VideoFilter.BLACK_AND_WHITE -> filters.add("format=gray")
            VideoFilter.SEPIA -> filters.add("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131")
            VideoFilter.VINTAGE -> filters.add("curves=r='0/0.11 0.42/0.51 1/0.95':g='0/0 0.50/0.48 1/1':b='0/0.22 0.49/0.44 1/0.8'")
            VideoFilter.NONE -> {}
        }

        // Add adjustments
        if (clip.brightness != 0f) {
            filters.add("brightness=${clip.brightness}")
        }
        if (clip.contrast != 1f) {
            filters.add("contrast=${clip.contrast}")
        }
        if (clip.saturation != 1f) {
            filters.add("saturation=${clip.saturation}")
        }

        val vf = if (filters.isNotEmpty()) "-vf \"${filters.joinToString(",")}\"" else ""

        // Speed adjustment
        val speedVideo = if (clip.playbackSpeed != 1f) "-filter:v \"setpts=PTS/${clip.playbackSpeed}\"" else ""
        val speedAudio = if (clip.playbackSpeed != 1f) "-filter:a \"atempo=${clip.playbackSpeed}\"" else ""

        val command = "-i \"$inputPath\" $vf $speedVideo $speedAudio -c:v libx264 -c:a aac \"$outputPath\""

        return command.trim()
    }
}