package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: HomeRepository,
) : ViewModel() {

    private val _programs = MutableStateFlow<List<ProgramItem>>(emptyList())

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val filteredPrograms: StateFlow<List<ProgramItem>> = combine(
        _programs, _selectedCategory
    ) { programs, category ->
        val withCoords = programs.filter { it.latitude != null && it.longitude != null }
        if (category == "all") withCoords
        else withCoords.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchPrograms() {
        if (_isLoading.value) return
        // 이미 데이터가 있으면 재로드 불필요
        if (_programs.value.isNotEmpty()) return
        _isLoading.value = true

        viewModelScope.launch {
            repository.getPrograms()
                .onSuccess { _programs.value = it }
                .onFailure { println("[MapVM] 프로그램 로드 실패: $it") }
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
