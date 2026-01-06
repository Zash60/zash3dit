package com.zash3dit.domain.usecase

import com.zash3dit.domain.model.TextOverlay
import com.zash3dit.domain.repository.EditProjectRepository

class AddTextOverlayUseCase(private val repository: EditProjectRepository) {
    suspend operator fun invoke(overlay: TextOverlay): Long {
        return repository.addTextOverlay(overlay)
    }
}