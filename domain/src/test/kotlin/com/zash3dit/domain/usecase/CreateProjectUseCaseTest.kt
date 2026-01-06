package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreateProjectUseCaseTest {

    private val repository: EditProjectRepository = mock()
    private val useCase = CreateProjectUseCase(repository)

    @Test
    fun `invoke creates project with default values and inserts it`() = runTest {
        // Given
        val name = "Test Project"
        val expectedId = 123L
        whenever(repository.insertProject(any())).thenReturn(expectedId)

        // When
        val result = useCase(name)

        // Then
        assertEquals(expectedId, result)
        val captor = argumentCaptor<EditProject>()
        verify(repository).insertProject(captor.capture())
        val capturedProject = captor.firstValue
        assertEquals(0L, capturedProject.id)
        assertEquals(name, capturedProject.name)
        assertEquals("1920x1080", capturedProject.resolution)
        assertEquals(30, capturedProject.frameRate)
        assertEquals(capturedProject.createdAt, capturedProject.modifiedAt)
    }

    @Test
    fun `invoke creates project with custom values and inserts it`() = runTest {
        // Given
        val name = "Custom Project"
        val resolution = "1280x720"
        val frameRate = 24
        val expectedId = 456L
        whenever(repository.insertProject(any())).thenReturn(expectedId)

        // When
        val result = useCase(name, resolution, frameRate)

        // Then
        assertEquals(expectedId, result)
        val captor = argumentCaptor<EditProject>()
        verify(repository).insertProject(captor.capture())
        val capturedProject = captor.firstValue
        assertEquals(0L, capturedProject.id)
        assertEquals(name, capturedProject.name)
        assertEquals(resolution, capturedProject.resolution)
        assertEquals(frameRate, capturedProject.frameRate)
        assertEquals(capturedProject.createdAt, capturedProject.modifiedAt)
    }
}