package com.zash3dit.presentation.viewmodel

import app.cash.turbine.test
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.usecase.CreateProjectUseCase
import com.zash3dit.domain.usecase.GetProjectsUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HomeViewModelTest {

    private val getProjectsUseCase: GetProjectsUseCase = mock()
    private val createProjectUseCase: CreateProjectUseCase = mock()
    private val viewModel = HomeViewModel(getProjectsUseCase, createProjectUseCase)

    @Test
    fun `init loads projects`() = runTest {
        // Given
        val projects = listOf(EditProject(1L, "Project 1", 1000L, 1000L, "1920x1080", 30))
        whenever(getProjectsUseCase()).thenReturn(flowOf(projects))

        // When & Then
        viewModel.projects.test {
            assertEquals(projects, awaitItem())
        }
    }

    @Test
    fun `createProject calls useCase and reloads projects`() = runTest {
        // Given
        val name = "New Project"
        val projects = listOf(EditProject(1L, "Project 1", 1000L, 1000L, "1920x1080", 30))
        whenever(getProjectsUseCase()).thenReturn(flowOf(emptyList()), flowOf(projects))
        whenever(createProjectUseCase(name)).thenReturn(1L)

        // When
        viewModel.createProject(name)

        // Then
        verify(createProjectUseCase).invoke(name)
        // Since it's async, we can check that projects flow emits the new list
        viewModel.projects.test {
            skipItems(1) // skip initial empty
            assertEquals(projects, awaitItem())
        }
    }
}