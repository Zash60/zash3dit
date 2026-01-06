package com.zash3dit.data.mapper;

import com.zash3dit.data.local.entity.AudioClipEntity;
import com.zash3dit.data.local.entity.EditProjectEntity;
import com.zash3dit.data.local.entity.TextOverlayEntity;
import com.zash3dit.data.local.entity.VideoClipEntity;
import com.zash3dit.domain.model.AudioClip;
import com.zash3dit.domain.model.EditProject;
import com.zash3dit.domain.model.TextOverlay;
import com.zash3dit.domain.model.VideoClip;
import com.zash3dit.domain.model.TransitionType;
import com.zash3dit.domain.model.VideoFilter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EditProjectMapperTest {

    @Test
    public void testEditProjectEntityToDomain() {
        // Given
        EditProjectEntity entity = new EditProjectEntity(1L, "Test Project", 1000L, 2000L, "1920x1080", 30);
        List<VideoClipEntity> videoClips = Collections.singletonList(
                new VideoClipEntity(1L, 1L, "/path/video.mp4", 0L, 5000L, 0, 0L, 0L, "NONE", 0f, 1f, 1f, 1f, "NONE", 1000L)
        );
        List<AudioClipEntity> audioClips = Collections.emptyList();
        List<TextOverlayEntity> textOverlays = Collections.emptyList();

        // When
        EditProject domain = entity.toDomain(videoClips, audioClips, textOverlays);

        // Then
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getName(), domain.getName());
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(entity.getModifiedAt(), domain.getModifiedAt());
        assertEquals(entity.getResolution(), domain.getResolution());
        assertEquals(entity.getFrameRate(), domain.getFrameRate());
        assertEquals(1, domain.getVideoClips().size());
        assertEquals(0, domain.getAudioClips().size());
        assertEquals(0, domain.getTextOverlays().size());
    }

    @Test
    public void testEditProjectToEntity() {
        // Given
        EditProject domain = new EditProject(1L, "Test Project", 1000L, 2000L, "1920x1080", 30, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        // When
        EditProjectEntity entity = domain.toEntity();

        // Then
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getName(), entity.getName());
        assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
        assertEquals(domain.getModifiedAt(), entity.getModifiedAt());
        assertEquals(domain.getResolution(), entity.getResolution());
        assertEquals(domain.getFrameRate(), entity.getFrameRate());
    }

    @Test
    public void testVideoClipEntityToDomain() {
        // Given
        VideoClipEntity entity = new VideoClipEntity(1L, 1L, "/path/video.mp4", 0L, 5000L, 0, 0L, 0L, "BLACK_AND_WHITE", 0.5f, 1.2f, 0.8f, 1.5f, "FADE_IN_OUT", 2000L);

        // When
        VideoClip domain = entity.toDomain();

        // Then
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getProjectId(), domain.getProjectId());
        assertEquals(entity.getFilePath(), domain.getFilePath());
        assertEquals(entity.getStartTime(), domain.getStartTime());
        assertEquals(entity.getDuration(), domain.getDuration());
        assertEquals(entity.getPosition(), domain.getPosition());
        assertEquals(entity.getTrimStart(), domain.getTrimStart());
        assertEquals(entity.getTrimEnd(), domain.getTrimEnd());
        assertEquals(VideoFilter.BLACK_AND_WHITE, domain.getFilter());
        assertEquals(entity.getBrightness(), domain.getBrightness(), 0.0f);
        assertEquals(entity.getContrast(), domain.getContrast(), 0.0f);
        assertEquals(entity.getSaturation(), domain.getSaturation(), 0.0f);
        assertEquals(entity.getPlaybackSpeed(), domain.getPlaybackSpeed(), 0.0f);
        assertEquals(TransitionType.FADE_IN_OUT, domain.getTransitionType());
        assertEquals(entity.getTransitionDuration(), domain.getTransitionDuration());
    }

    @Test
    public void testVideoClipToEntity() {
        // Given
        VideoClip domain = new VideoClip(1L, 1L, "/path/video.mp4", 0L, 5000L, 0, 0L, 0L, VideoFilter.SEPIA, 0.5f, 1.2f, 0.8f, 1.5f, TransitionType.SLIDE, 2000L);

        // When
        VideoClipEntity entity = domain.toEntity();

        // Then
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getProjectId(), entity.getProjectId());
        assertEquals(domain.getFilePath(), entity.getFilePath());
        assertEquals(domain.getStartTime(), entity.getStartTime());
        assertEquals(domain.getDuration(), entity.getDuration());
        assertEquals(domain.getPosition(), entity.getPosition());
        assertEquals(domain.getTrimStart(), entity.getTrimStart());
        assertEquals(domain.getTrimEnd(), entity.getTrimEnd());
        assertEquals(domain.getFilter().name(), entity.getFilter());
        assertEquals(domain.getBrightness(), entity.getBrightness(), 0.0f);
        assertEquals(domain.getContrast(), entity.getContrast(), 0.0f);
        assertEquals(domain.getSaturation(), entity.getSaturation(), 0.0f);
        assertEquals(domain.getPlaybackSpeed(), entity.getPlaybackSpeed(), 0.0f);
        assertEquals(domain.getTransitionType().name(), entity.getTransitionType());
        assertEquals(domain.getTransitionDuration(), entity.getTransitionDuration());
    }

    @Test
    public void testAudioClipEntityToDomain() {
        // Given
        AudioClipEntity entity = new AudioClipEntity(1L, 1L, "/path/audio.mp3", 0L, 5000L, 0, 1.0f);

        // When
        AudioClip domain = entity.toDomain();

        // Then
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getProjectId(), domain.getProjectId());
        assertEquals(entity.getFilePath(), domain.getFilePath());
        assertEquals(entity.getStartTime(), domain.getStartTime());
        assertEquals(entity.getDuration(), domain.getDuration());
        assertEquals(entity.getPosition(), domain.getPosition());
        assertEquals(entity.getVolume(), domain.getVolume(), 0.0f);
    }

    @Test
    public void testAudioClipToEntity() {
        // Given
        AudioClip domain = new AudioClip(1L, 1L, "/path/audio.mp3", 0L, 5000L, 0, 0.8f);

        // When
        AudioClipEntity entity = domain.toEntity();

        // Then
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getProjectId(), entity.getProjectId());
        assertEquals(domain.getFilePath(), entity.getFilePath());
        assertEquals(domain.getStartTime(), entity.getStartTime());
        assertEquals(domain.getDuration(), entity.getDuration());
        assertEquals(domain.getPosition(), entity.getPosition());
        assertEquals(domain.getVolume(), entity.getVolume(), 0.0f);
    }

    @Test
    public void testTextOverlayEntityToDomain() {
        // Given
        TextOverlayEntity entity = new TextOverlayEntity(1L, 1L, "Hello World", 0L, 5000L, 0, 100f, 200f, 24f, "#FFFFFF");

        // When
        TextOverlay domain = entity.toDomain();

        // Then
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getProjectId(), domain.getProjectId());
        assertEquals(entity.getText(), domain.getText());
        assertEquals(entity.getStartTime(), domain.getStartTime());
        assertEquals(entity.getDuration(), domain.getDuration());
        assertEquals(entity.getPosition(), domain.getPosition());
        assertEquals(entity.getX(), domain.getX(), 0.0f);
        assertEquals(entity.getY(), domain.getY(), 0.0f);
        assertEquals(entity.getFontSize(), domain.getFontSize(), 0.0f);
        assertEquals(entity.getColor(), domain.getColor());
    }

    @Test
    public void testTextOverlayToEntity() {
        // Given
        TextOverlay domain = new TextOverlay(1L, 1L, "Hello World", 0L, 5000L, 0, 100f, 200f, 24f, "#FFFFFF");

        // When
        TextOverlayEntity entity = domain.toEntity();

        // Then
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getProjectId(), entity.getProjectId());
        assertEquals(domain.getText(), entity.getText());
        assertEquals(domain.getStartTime(), entity.getStartTime());
        assertEquals(domain.getDuration(), entity.getDuration());
        assertEquals(domain.getPosition(), entity.getPosition());
        assertEquals(domain.getX(), entity.getX(), 0.0f);
        assertEquals(domain.getY(), entity.getY(), 0.0f);
        assertEquals(domain.getFontSize(), entity.getFontSize(), 0.0f);
        assertEquals(domain.getColor(), entity.getColor());
    }
}