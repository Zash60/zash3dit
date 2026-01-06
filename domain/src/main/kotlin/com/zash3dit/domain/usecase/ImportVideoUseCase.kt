package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository

class ImportVideoUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(projectId: Long, filePath: String, duration: Long = 0L) {
        val project = repository.getProjectById(projectId) ?: return
        val position = project.videoClips.size
        val startTime = project.videoClips.sumOf { it.duration }
        val newClip = VideoClip(
            projectId = projectId,
            filePath = filePath,
            startTime = startTime,
            duration = duration,
            position = position
        )
        val updatedProject = project.copy(videoClips = project.videoClips + newClip)
        repository.updateProject(updatedProject)
    }
}