package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.AudioClip
import com.zash3dit.domain.repository.EditProjectRepository

class ImportAudioUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(projectId: Long, filePath: String, duration: Long = 0L) {
        val project = repository.getProjectById(projectId) ?: return
        val position = project.audioClips.size
        val startTime = 0L // Audio clips can start at any time, default to 0
        val newClip = AudioClip(
            projectId = projectId,
            filePath = filePath,
            startTime = startTime,
            duration = duration,
            position = position
        )
        val updatedProject = project.copy(audioClips = project.audioClips + newClip)
        repository.updateProject(updatedProject)
    }
}