package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository

class ApplyAudioMixingUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(
        projectId: Long,
        videoClipId: Long,
        outputPath: String
    ): String {
        val project = repository.getProjectById(projectId) ?: throw IllegalArgumentException("Project not found")

        val videoClip = project.videoClips.find { it.id == videoClipId }
            ?: throw IllegalArgumentException("Video clip not found")

        // Get all audio clips that overlap with the video clip's time range
        val overlappingAudioClips = project.audioClips.filter { audioClip ->
            // Check if audio clip overlaps with video clip
            audioClip.startTime < videoClip.startTime + videoClip.duration &&
            audioClip.startTime + audioClip.duration > videoClip.startTime
        }

        if (overlappingAudioClips.isEmpty()) {
            // No audio to mix, just copy the video
            return "-i \"${videoClip.filePath}\" -c copy \"$outputPath\""
        }

        // Build FFmpeg command with amix filter
        val inputs = mutableListOf<String>()

        // Add video input
        inputs.add("-i \"${videoClip.filePath}\"")

        // Add audio inputs
        overlappingAudioClips.forEach { audioClip ->
            inputs.add("-i \"${audioClip.filePath}\"")
        }

        // For simplicity, mix all audio clips into one audio stream and combine with video
        // This is a simplified version - in a real app you'd want more sophisticated mixing

        if (overlappingAudioClips.size == 1) {
            // Simple case: one audio clip
            val audioClip = overlappingAudioClips.first()
            val delayMs = maxOf(0L, audioClip.startTime - videoClip.startTime)
            val delay = if (delayMs > 0) "adelay=${delayMs}|${delayMs}," else ""
            val volume = if (audioClip.volume != 1.0f) "volume=${audioClip.volume}," else ""

            val command = "-i \"${videoClip.filePath}\" -i \"${audioClip.filePath}\" -filter_complex \"[${delay}${volume}amix=inputs=2:duration=first[aout]\" -map 0:v -map \"[aout]\" -c:v copy -c:a aac -shortest \"$outputPath\""
            return command
        } else {
            // Multiple audio clips - for now, just use the first one
            // In a real implementation, you'd mix them properly
            val audioClip = overlappingAudioClips.first()
            val delayMs = maxOf(0L, audioClip.startTime - videoClip.startTime)
            val delay = if (delayMs > 0) "adelay=${delayMs}|${delayMs}," else ""
            val volume = if (audioClip.volume != 1.0f) "volume=${audioClip.volume}," else ""

            val command = "-i \"${videoClip.filePath}\" -i \"${audioClip.filePath}\" -filter_complex \"[${delay}${volume}amix=inputs=2:duration=first[aout]\" -map 0:v -map \"[aout]\" -c:v copy -c:a aac -shortest \"$outputPath\""
            return command
        }
    }
}