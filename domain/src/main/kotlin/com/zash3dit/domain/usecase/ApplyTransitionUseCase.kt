package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.TransitionType
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository

class ApplyTransitionUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        clipIds: List<Long>,
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")

        if (clipIds.size < 2) {
            throw IllegalArgumentException("At least 2 clips required for transitions")
        }

        val clips = clipIds.map { id ->
            project.videoClips.find { it.id == id } ?: throw IllegalArgumentException("Clip $id not found")
        }.sortedBy { it.position }

        // Build FFmpeg command with transitions using xfade filter
        val inputs = clips.joinToString(" ") { "-i \"${it.filePath}\"" }

        val transitions = mutableListOf<String>()
        val transitionOffsets = mutableListOf<String>()

        for (i in 0 until clips.size - 1) {
            val currentClip = clips[i]
            val transitionType = currentClip.transitionType
            val duration = currentClip.transitionDuration / 1000f // Convert to seconds

            if (transitionType != TransitionType.NONE) {
                val transitionFilter = when (transitionType) {
                    TransitionType.FADE_IN_OUT -> "fade"
                    TransitionType.SLIDE -> "wipe"
                    TransitionType.DISSOLVE -> "dissolve"
                    TransitionType.NONE -> ""
                }

                val startTime = (currentClip.duration - currentClip.transitionDuration) / 1000f
                transitions.add("[$i:v][$i+1:v]xfade=transition=$transitionFilter:duration=$duration:offset=$startTime[v$i];")
                transitionOffsets.add("[v$i]")
            }
        }

        val filterComplex = transitions.joinToString("") + transitionOffsets.joinToString("") { it }

        val command = "$inputs -filter_complex \"$filterComplex concat=n=${clips.size}:v=1:a=1\" -c:v libx264 -c:a aac \"$outputPath\""

        return command.trim()
    }
}