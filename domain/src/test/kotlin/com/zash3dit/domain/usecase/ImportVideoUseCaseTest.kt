package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ImportVideoUseCaseTest {

    private val repository: EditProjectRepository = mock()
    private val useCase = ImportVideoUseCase(repository)

    @Test
    fun `invoke adds video clip to project with no existing clips`() = runTest {
        // Given
        val projectId = 1L
        val filePath = "/path/to/video.mp4"
        val duration = 5000L
        val project = EditProject(id = projectId, name = "Test Project", createdAt = 1000, modifiedAt = 1000, resolution = "1920x1080", frameRate = 30, videoClips = emptyList())
        whenever(repository.getProjectById(projectId)).thenReturn(project)

        // When
        useCase(projectId, filePath, duration)

        // Then
        val captor = argumentCaptor<EditProject>()
        verify(repository).updateProject(captor.capture())
        val updatedProject = captor.firstValue
        assert(updatedProject.videoClips.size == 1)
        val clip = updatedProject.videoClips[0]
        assert(clip.projectId == projectId)
        assert(clip.filePath == filePath)
        assert(clip.startTime == 0L)
        assert(clip.duration == duration)
        assert(clip.position == 0)
    }

    @Test
    fun `invoke adds video clip to project with existing clips`() = runTest {
        // Given
        val projectId = 1L
        val filePath = "/path/to/video2.mp4"
        val duration = 3000L
        val existingClip = VideoClip(id = 1, projectId = projectId, filePath = "/path/to/video1.mp4", startTime = 0, duration = 5000, position = 0)
        val project = EditProject(id = projectId, name = "Test Project", createdAt = 1000, modifiedAt = 1000, resolution = "1920x1080", frameRate = 30, videoClips = listOf(existingClip))
        whenever(repository.getProjectById(projectId)).thenReturn(project)

        // When
        useCase(projectId, filePath, duration)

        // Then
        val captor = argumentCaptor<EditProject>()
        verify(repository).updateProject(captor.capture())
        val updatedProject = captor.firstValue
        assert(updatedProject.videoClips.size == 2)
        val newClip = updatedProject.videoClips[1]
        assert(newClip.projectId == projectId)
        assert(newClip.filePath == filePath)
        assert(newClip.startTime == 5000L) // sum of previous durations
        assert(newClip.duration == duration)
        assert(newClip.position == 1)
    }

    @Test
    fun `invoke does nothing when project not found`() = runTest {
        // Given
        val projectId = 999L
        val filePath = "/path/to/video.mp4"
        val duration = 5000L
        whenever(repository.getProjectById(projectId)).thenReturn(null)

        // When
        useCase(projectId, filePath, duration)

        // Then
        verify(repository, never()).updateProject(any())
    }
}