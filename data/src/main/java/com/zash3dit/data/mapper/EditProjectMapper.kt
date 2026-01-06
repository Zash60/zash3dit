package com.zash3dit.data.mapper

import com.zash3dit.data.local.entity.AudioClipEntity
import com.zash3dit.data.local.entity.EditProjectEntity
import com.zash3dit.data.local.entity.TextOverlayEntity
import com.zash3dit.data.local.entity.VideoClipEntity
import com.zash3dit.domain.model.AudioClip
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.TextOverlay
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.model.TransitionType
import com.zash3dit.domain.model.VideoFilter

fun EditProjectEntity.toDomain(
    videoClips: List<VideoClipEntity> = emptyList(),
    audioClips: List<AudioClipEntity> = emptyList(),
    textOverlays: List<TextOverlayEntity> = emptyList()
) = EditProject(
    id = id,
    name = name,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    resolution = resolution,
    frameRate = frameRate,
    videoClips = videoClips.map { it.toDomain() },
    audioClips = audioClips.map { it.toDomain() },
    textOverlays = textOverlays.map { it.toDomain() }
)

fun EditProject.toEntity() = EditProjectEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    resolution = resolution,
    frameRate = frameRate
)

fun VideoClipEntity.toDomain() = VideoClip(
    id = id,
    projectId = projectId,
    filePath = filePath,
    startTime = startTime,
    duration = duration,
    position = position,
    trimStart = trimStart,
    trimEnd = trimEnd,
    filter = VideoFilter.valueOf(filter),
    brightness = brightness,
    contrast = contrast,
    saturation = saturation,
    playbackSpeed = playbackSpeed,
    transitionType = TransitionType.valueOf(transitionType),
    transitionDuration = transitionDuration
)

fun VideoClip.toEntity() = VideoClipEntity(
    id = id,
    projectId = projectId,
    filePath = filePath,
    startTime = startTime,
    duration = duration,
    position = position,
    trimStart = trimStart,
    trimEnd = trimEnd,
    filter = filter.name,
    brightness = brightness,
    contrast = contrast,
    saturation = saturation,
    playbackSpeed = playbackSpeed,
    transitionType = transitionType.name,
    transitionDuration = transitionDuration
)

fun AudioClipEntity.toDomain() = AudioClip(
    id = id,
    projectId = projectId,
    filePath = filePath,
    startTime = startTime,
    duration = duration,
    position = position,
    volume = volume
)

fun AudioClip.toEntity() = AudioClipEntity(
    id = id,
    projectId = projectId,
    filePath = filePath,
    startTime = startTime,
    duration = duration,
    position = position,
    volume = volume
)

fun TextOverlayEntity.toDomain() = TextOverlay(
    id = id,
    projectId = projectId,
    text = text,
    startTime = startTime,
    duration = duration,
    position = position,
    x = x,
    y = y,
    fontSize = fontSize,
    color = color
)

fun TextOverlay.toEntity() = TextOverlayEntity(
    id = id,
    projectId = projectId,
    text = text,
    startTime = startTime,
    duration = duration,
    position = position,
    x = x,
    y = y,
    fontSize = fontSize,
    color = color
)