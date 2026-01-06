package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository

class CreateProjectUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(name: String, resolution: String = "1920x1080", frameRate: Int = 30): Long {
        val currentTime = System.currentTimeMillis()
        val project = EditProject(
            id = 0,
            name = name,
            createdAt = currentTime,
            modifiedAt = currentTime,
            resolution = resolution,
            frameRate = frameRate
        )
        return repository.insertProject(project)
    }
}