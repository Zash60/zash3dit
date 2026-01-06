package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateProjectUseCaseTest {

    private val repository: EditProjectRepository = mock()
    private val useCase = UpdateProjectUseCase(repository)

    @Test
    fun `invoke calls repository updateProject with given project`() = runTest {
        // Given
        val project = EditProject(id = 1, name = "Updated Project", createdAt = 1000, modifiedAt = 2000, resolution = "1920x1080", frameRate = 30)

        // When
        useCase(project)

        // Then
        verify(repository).updateProject(project)
    }
}