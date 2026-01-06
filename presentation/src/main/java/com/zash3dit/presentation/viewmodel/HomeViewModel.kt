package com.zash3dit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.ErrorState
import com.zash3dit.domain.usecase.CreateProjectUseCase
import com.zash3dit.domain.usecase.GetProjectsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    private val _projects = MutableStateFlow<List<EditProject>>(emptyList())
    val projects: StateFlow<List<EditProject>> = _projects

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorState = MutableStateFlow(ErrorState())
    val errorState: StateFlow<ErrorState> = _errorState

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            getProjectsUseCase().collect { projects ->
                _projects.value = projects
                _isLoading.value = false
            }
        }
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            createProjectUseCase(name)
            // After creating, reload projects
            loadProjects()
        }
    }
}