package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetProjectByIdUseCaseTest {

    private val repository: EditProjectRepository = mock()
    private val useCase = GetProjectByIdUseCase(repository)

    @Test
    fun `invoke returns project when found`() = runTest {
        // Given
        val id = 1L
        val project = EditProject(id = id, name = "Test Project", createdAt = 1000, modifiedAt = 1000, resolution = "1920x1080", frameRate = 30)
        whenever(repository.getProjectById(id)).thenReturn(project)

        // When
        val result = useCase(id)

        // Then
        assertEquals(project, result)
        verify(repository).getProjectById(id)
    }

    @Test
    fun `invoke returns null when project not found`() = runTest {
        // Given
        val id = 999L
        whenever(repository.getProjectById(id)).thenReturn(null)

        // When
        val result = useCase(id)

        // Then
        assertNull(result)
        verify(repository).getProjectById(id)
    }
}