package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository

class TrimVideoUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        clipId: Long,
        startTime: Long, // in milliseconds
        endTime: Long,   // in milliseconds
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")
        val clip = project.videoClips.find { it.id == clipId } ?: throw IllegalArgumentException("Clip not found")

        // Validate trim times
        if (startTime < 0 || endTime > clip.duration || startTime >= endTime) {
            throw IllegalArgumentException("Invalid trim times")
        }

        val trimmedDuration = endTime - startTime

        // Create FFmpeg command for trimming
        val inputPath = clip.filePath
        val command = "-i \"$inputPath\" -ss ${startTime / 1000.0} -t ${trimmedDuration / 1000.0} -c copy \"$outputPath\""

        // Note: Actual FFmpeg execution will be handled in ViewModel
        return command
    }
}