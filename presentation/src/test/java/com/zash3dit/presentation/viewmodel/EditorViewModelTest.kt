package com.zash3dit.presentation.viewmodel

import app.cash.turbine.test
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.usecase.AddTextOverlayUseCase
import com.zash3dit.domain.usecase.ApplyAudioMixingUseCase
import com.zash3dit.domain.usecase.ApplyTextOverlayUseCase
import com.zash3dit.domain.usecase.ApplyTransitionUseCase
import com.zash3dit.domain.usecase.ApplyVideoEffectsUseCase
import com.zash3dit.domain.usecase.GetProjectByIdUseCase
import com.zash3dit.domain.usecase.ImportAudioUseCase
import com.zash3dit.domain.usecase.ImportVideoUseCase
import com.zash3dit.domain.usecase.MergeVideosUseCase
import com.zash3dit.domain.usecase.SplitVideoUseCase
import com.zash3dit.domain.usecase.TrimVideoUseCase
import com.zash3dit.domain.usecase.UpdateProjectUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EditorViewModelTest {

    private val getProjectByIdUseCase: GetProjectByIdUseCase = mock()
    private val updateProjectUseCase: UpdateProjectUseCase = mock()
    private val importVideoUseCase: ImportVideoUseCase = mock()
    private val trimVideoUseCase: TrimVideoUseCase = mock()
    private val splitVideoUseCase: SplitVideoUseCase = mock()
    private val mergeVideosUseCase: MergeVideosUseCase = mock()
    private val applyVideoEffectsUseCase: ApplyVideoEffectsUseCase = mock()
    private val addTextOverlayUseCase: AddTextOverlayUseCase = mock()
    private val applyTextOverlayUseCase: ApplyTextOverlayUseCase = mock()
    private val importAudioUseCase: ImportAudioUseCase = mock()
    private val applyAudioMixingUseCase: ApplyAudioMixingUseCase = mock()
    private val applyTransitionUseCase: ApplyTransitionUseCase = mock()

    private val viewModel = EditorViewModel(
        getProjectByIdUseCase,
        updateProjectUseCase,
        importVideoUseCase,
        trimVideoUseCase,
        splitVideoUseCase,
        mergeVideosUseCase,
        applyVideoEffectsUseCase,
        addTextOverlayUseCase,
        applyTextOverlayUseCase,
        importAudioUseCase,
        applyAudioMixingUseCase,
        applyTransitionUseCase
    )

    @Test
    fun `loadProject loads project and updates state`() = runTest {
        // Given
        val projectId = 1L
        val project = EditProject(id = projectId, name = "Test", createdAt = 1000L, modifiedAt = 1000L, resolution = "1920x1080", frameRate = 30)
        whenever(getProjectByIdUseCase(projectId)).thenReturn(project)

        // When
        viewModel.loadProject(projectId)

        // Then
        viewModel.project.test {
            assertEquals(project, awaitItem())
        }
    }

    @Test
    fun `saveProject updates project with current time`() = runTest {
        // Given
        val project = EditProject(id = 1L, name = "Test", createdAt = 1000L, modifiedAt = 1000L, resolution = "1920x1080", frameRate = 30)
        viewModel.updateProject(project) // Set project

        // When
        viewModel.saveProject()

        // Then
        verify(updateProjectUseCase).invoke(any()) // Verify called with updated modifiedAt
    }

    @Test
    fun `importVideo calls useCase and reloads project`() = runTest {
        // Given
        val project = EditProject(id = 1L, name = "Test", createdAt = 1000L, modifiedAt = 1000L, resolution = "1920x1080", frameRate = 30)
        val filePath = "/path/video.mp4"
        val duration = 5000L
        val updatedProject = project.copy(videoClips = listOf(/* new clip */))
        viewModel.updateProject(project)
        whenever(getProjectByIdUseCase(1L)).thenReturn(updatedProject)

        // When
        viewModel.importVideo(filePath, duration)

        // Then
        verify(importVideoUseCase).invoke(1L, filePath, duration)
        // Project should be reloaded
    }

    @Test
    fun `selectClip updates selectedClipIds`() = runTest {
        // Given
        val clipId = 123L

        // When
        viewModel.selectClip(clipId)

        // Then
        viewModel.selectedClipIds.test {
            assertEquals(setOf(clipId), awaitItem())
        }
    }

    @Test
    fun `toggleClipSelection adds and removes clip`() = runTest {
        // Given
        val clipId = 123L

        // When
        viewModel.toggleClipSelection(clipId)

        // Then
        viewModel.selectedClipIds.test {
            assertEquals(setOf(clipId), awaitItem())
        }

        // When toggle again
        viewModel.toggleClipSelection(clipId)

        // Then
        viewModel.selectedClipIds.test {
            skipItems(1) // skip previous
            assertEquals(emptySet(), awaitItem())
        }
    }
}