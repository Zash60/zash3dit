package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.flow.Flow

class GetProjectsUseCase(private val repository: EditProjectRepository) {
    operator fun invoke(): Flow<List<EditProject>> = repository.getAllProjects()
}