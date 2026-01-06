package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.TransitionType
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository

class MergeVideosUseCase(
    private val repository: EditProjectRepository,
    private val applyTransitionUseCase: ApplyTransitionUseCase
) {
    suspend operator fun invoke(
        projectId: Long,
        clipIds: List<Long>,
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")

        if (clipIds.size < 2) {
            throw IllegalArgumentException("At least 2 clips required for merging")
        }

        val clips = clipIds.map { id ->
            project.videoClips.find { it.id == id } ?: throw IllegalArgumentException("Clip $id not found")
        }

        // Check if any clip has transitions
        val hasTransitions = clips.any { it.transitionType != TransitionType.NONE }

        return if (hasTransitions) {
            applyTransitionUseCase(projectId, clipIds, outputPath)
        } else {
            // Create FFmpeg command for merging without transitions
            val inputPaths = clips.joinToString(" ") { "\"${it.filePath}\"" }
            val concatCommand = "-i \"$inputPaths\" -filter_complex \"concat=n=${clips.size}:v=1:a=1\" -c:v libx264 -c:a aac \"$outputPath\""

            // Note: Actual FFmpeg execution will be handled in ViewModel
            concatCommand
        }
    }
}