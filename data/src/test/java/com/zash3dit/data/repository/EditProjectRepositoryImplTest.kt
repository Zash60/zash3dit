package com.zash3dit.data.repository

import app.cash.turbine.test
import com.zash3dit.data.local.AppDatabase
import com.zash3dit.data.local.dao.AudioClipDao
import com.zash3dit.data.local.dao.EditProjectDao
import com.zash3dit.data.local.dao.TextOverlayDao
import com.zash3dit.data.local.dao.VideoClipDao
import com.zash3dit.data.local.entity.AudioClipEntity
import com.zash3dit.data.local.entity.EditProjectEntity
import com.zash3dit.data.local.entity.TextOverlayEntity
import com.zash3dit.data.local.entity.VideoClipEntity
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.TextOverlay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EditProjectRepositoryImplTest {

    private val database: AppDatabase = mock()
    private val projectDao: EditProjectDao = mock()
    private val videoClipDao: VideoClipDao = mock()
    private val audioClipDao: AudioClipDao = mock()
    private val textOverlayDao: TextOverlayDao = mock()

    private val repository = EditProjectRepositoryImpl(database).apply {
        // Since DAOs are private, we mock the database to return our mocks
        // But since it's private val, we can't inject, so we need to mock database methods
    }

    init {
        whenever(database.editProjectDao()).thenReturn(projectDao)
        whenever(database.videoClipDao()).thenReturn(videoClipDao)
        whenever(database.audioClipDao()).thenReturn(audioClipDao)
        whenever(database.textOverlayDao()).thenReturn(textOverlayDao)
        whenever(database.runInTransaction(any<() -> Long>())).thenAnswer { (it.arguments[0] as () -> Long)() }
        whenever(database.runInTransaction(any<() -> Unit>())).thenAnswer { (it.arguments[0] as () -> Unit)() }
    }

    @Test
    fun `getAllProjects returns flow of projects with clips and overlays`() = runTest {
        // Given
        val projectEntity = EditProjectEntity(1L, "Test", 1000L, 1000L, "1920x1080", 30)
        val projects = listOf(projectEntity)
        val videoClips = listOf(VideoClipEntity(1L, 1L, "/video.mp4", 0L, 5000L, 0, 0L, 0L, "NONE", 0f, 1f, 1f, 1f, "NONE", 1000L))
        val audioClips = emptyList<AudioClipEntity>()
        val textOverlays = emptyList<TextOverlayEntity>()

        whenever(projectDao.getAllProjects()).thenReturn(flowOf(projects))
        whenever(videoClipDao.getClipsForProject(1L)).thenReturn(flowOf(videoClips))
        whenever(audioClipDao.getClipsForProject(1L)).thenReturn(flowOf(audioClips))
        whenever(textOverlayDao.getOverlaysForProject(1L)).thenReturn(flowOf(textOverlays))

        // When & Then
        repository.getAllProjects().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            val project = result[0]
            assertEquals(1L, project.id)
            assertEquals("Test", project.name)
            assertEquals(1, project.videoClips.size)
            awaitComplete()
        }
    }

    @Test
    fun `getProjectById returns project with clips when found`() = runTest {
        // Given
        val projectEntity = EditProjectEntity(1L, "Test", 1000L, 1000L, "1920x1080", 30)
        val videoClips = listOf(VideoClipEntity(1L, 1L, "/video.mp4", 0L, 5000L, 0, 0L, 0L, "NONE", 0f, 1f, 1f, 1f, "NONE", 1000L))
        val audioClips = emptyList<AudioClipEntity>()
        val textOverlays = emptyList<TextOverlayEntity>()

        whenever(projectDao.getProjectById(1L)).thenReturn(projectEntity)
        whenever(videoClipDao.getClipsForProject(1L)).thenReturn(flowOf(videoClips))
        whenever(audioClipDao.getClipsForProject(1L)).thenReturn(flowOf(audioClips))
        whenever(textOverlayDao.getOverlaysForProject(1L)).thenReturn(flowOf(textOverlays))

        // When
        val result = repository.getProjectById(1L)

        // Then
        assertEquals(1L, result?.id)
        assertEquals("Test", result?.name)
        assertEquals(1, result?.videoClips?.size)
    }

    @Test
    fun `getProjectById returns null when not found`() = runTest {
        // Given
        whenever(projectDao.getProjectById(1L)).thenReturn(null)

        // When
        val result = repository.getProjectById(1L)

        // Then
        assertNull(result)
    }

    @Test
    fun `insertProject inserts project and clips`() = runTest {
        // Given
        val project = EditProject(0L, "New Project", 1000L, 1000L, "1920x1080", 30, emptyList(), emptyList(), emptyList())
        val insertedId = 123L
        whenever(projectDao.insertProject(any())).thenReturn(insertedId)

        // When
        val result = repository.insertProject(project)

        // Then
        assertEquals(insertedId, result)
        val captor = argumentCaptor<EditProjectEntity>()
        verify(projectDao).insertProject(captor.capture())
        val entity = captor.firstValue
        assertEquals("New Project", entity.name)
    }

    @Test
    fun `updateProject updates project and replaces clips`() = runTest {
        // Given
        val project = EditProject(1L, "Updated", 1000L, 2000L, "1920x1080", 30, emptyList(), emptyList(), emptyList())

        // When
        repository.updateProject(project)

        // Then
        verify(projectDao).updateProject(any())
        verify(videoClipDao).deleteClipsForProject(1L)
        verify(audioClipDao).deleteClipsForProject(1L)
        verify(textOverlayDao).deleteOverlaysForProject(1L)
    }

    @Test
    fun `addTextOverlay inserts overlay`() = runTest {
        // Given
        val overlay = TextOverlay(0L, 1L, "Text", 0L, 5000L, 0, 100f, 200f, 24f, "#FFF")
        val insertedId = 456L
        whenever(textOverlayDao.insertOverlay(any())).thenReturn(insertedId)

        // When
        val result = repository.addTextOverlay(overlay)

        // Then
        assertEquals(insertedId, result)
        verify(textOverlayDao).insertOverlay(any())
    }

    @Test
    fun `getTextOverlaysForProject returns flow of overlays`() = runTest {
        // Given
        val entities = listOf(TextOverlayEntity(1L, 1L, "Text", 0L, 5000L, 0, 100f, 200f, 24f, "#FFF"))
        whenever(textOverlayDao.getOverlaysForProject(1L)).thenReturn(flowOf(entities))

        // When & Then
        repository.getTextOverlaysForProject(1L).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Text", result[0].text)
            awaitComplete()
        }
    }
}