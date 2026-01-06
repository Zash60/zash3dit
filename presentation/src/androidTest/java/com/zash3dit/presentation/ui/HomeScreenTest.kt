package com.zash3dit.presentation.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.zash3dit.domain.model.EditProject
import com.zash3dit.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel: HomeViewModel = mock()

    @Test
    fun `displays projects list`() {
        // Given
        val projects = listOf(
            EditProject(1L, "Project 1", 1000L, 1000L, "1920x1080", 30),
            EditProject(2L, "Project 2", 2000L, 2000L, "1280x720", 24)
        )
        whenever(viewModel.projects).thenReturn(MutableStateFlow(projects))
        whenever(viewModel.isLoading).thenReturn(MutableStateFlow(false))

        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onProjectSelected = {})
        }

        // Then
        composeTestRule.onNodeWithText("Project 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Project 2").assertIsDisplayed()
    }

    @Test
    fun `shows loading indicator when loading`() {
        // Given
        whenever(viewModel.projects).thenReturn(MutableStateFlow(emptyList()))
        whenever(viewModel.isLoading).thenReturn(MutableStateFlow(true))

        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onProjectSelected = {})
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun `clicking FAB opens create dialog`() {
        // Given
        whenever(viewModel.projects).thenReturn(MutableStateFlow(emptyList()))
        whenever(viewModel.isLoading).thenReturn(MutableStateFlow(false))

        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onProjectSelected = {})
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Create Project").performClick()
        composeTestRule.onNodeWithText("Create New Project").assertIsDisplayed()
    }

    @Test
    fun `create project dialog creates project when confirmed`() {
        // Given
        whenever(viewModel.projects).thenReturn(MutableStateFlow(emptyList()))
        whenever(viewModel.isLoading).thenReturn(MutableStateFlow(false))

        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onProjectSelected = {})
        }

        // Open dialog
        composeTestRule.onNodeWithContentDescription("Create Project").performClick()
        // Enter name
        composeTestRule.onNodeWithText("Project Name").performTextInput("New Project")
        // Confirm
        composeTestRule.onNodeWithText("Create").performClick()

        // Then
        verify(viewModel).createProject("New Project")
    }

    @Test
    fun `clicking project item calls onProjectSelected`() {
        // Given
        val projects = listOf(EditProject(1L, "Project 1", 1000L, 1000L, "1920x1080", 30))
        var selectedId: Long? = null
        whenever(viewModel.projects).thenReturn(MutableStateFlow(projects))
        whenever(viewModel.isLoading).thenReturn(MutableStateFlow(false))

        // When
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onProjectSelected = { selectedId = it })
        }

        // Click project
        composeTestRule.onNodeWithText("Project 1").performClick()

        // Then
        assert(selectedId == 1L)
    }
}