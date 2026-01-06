package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository

class UpdateProjectUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(project: EditProject) = repository.updateProject(project)
}