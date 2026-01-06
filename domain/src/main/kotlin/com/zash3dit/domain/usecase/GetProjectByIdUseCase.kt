package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository

class GetProjectByIdUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(id: Long): EditProject? = repository.getProjectById(id)
}