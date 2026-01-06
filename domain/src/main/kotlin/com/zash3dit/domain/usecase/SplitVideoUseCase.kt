package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository

class SplitVideoUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        clipId: Long,
        splitTime: Long, // in milliseconds
        outputPath1: String,
        outputPath2: String
    ): Pair<String, String> {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")
        val clip = project.videoClips.find { it.id == clipId } ?: throw IllegalArgumentException("Clip not found")

        // Validate split time
        if (splitTime <= 0 || splitTime >= clip.duration) {
            throw IllegalArgumentException("Invalid split time")
        }

        // Create FFmpeg commands for splitting
        val inputPath = clip.filePath
        val command1 = "-i \"$inputPath\" -t ${splitTime / 1000.0} -c copy \"$outputPath1\""
        val command2 = "-i \"$inputPath\" -ss ${splitTime / 1000.0} -c copy \"$outputPath2\""

        // Note: Actual FFmpeg execution will be handled in ViewModel
        return Pair(command1, command2)
    }
}