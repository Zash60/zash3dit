package com.zash3dit.domain.model

sealed class AppError {
    data class PermissionError(val permission: String, val message: String) : AppError()
    data class FFmpegError(val operation: String, val message: String, val canRetry: Boolean = true) : AppError()
    data class FileError(val operation: String, val filePath: String?, val message: String) : AppError()
    data class StorageError(val message: String, val availableSpace: Long? = null) : AppError()
    data class DatabaseError(val operation: String, val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class UnknownError(val message: String) : AppError()
}

data class ErrorState(
    val error: AppError? = null,
    val isRecoverable: Boolean = false,
    val recoveryAction: (() -> Unit)? = null
) {
    val hasError: Boolean = error != null

    fun clear() = copy(error = null, recoveryAction = null)
}