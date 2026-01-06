package com.zash3dit.domain.usecase

import app.cash.turbine.test
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetProjectsUseCaseTest {

    private val repository: EditProjectRepository = mock()
    private val useCase = GetProjectsUseCase(repository)

    @Test
    fun `invoke returns flow from repository`() = runTest {
        // Given
        val projects = listOf(
            EditProject(id = 1, name = "Project 1", createdAt = 1000, modifiedAt = 1000, resolution = "1920x1080", frameRate = 30),
            EditProject(id = 2, name = "Project 2", createdAt = 2000, modifiedAt = 2000, resolution = "1280x720", frameRate = 24)
        )
        whenever(repository.getAllProjects()).thenReturn(flowOf(projects))

        // When & Then
        useCase().test {
            assertEquals(projects, awaitItem())
            awaitComplete()
        }
    }
}