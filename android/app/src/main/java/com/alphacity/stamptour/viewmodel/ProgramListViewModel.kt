package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramListViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _programs = MutableStateFlow<List<ProgramItem>>(emptyList())
    val programs: StateFlow<List<ProgramItem>> = _programs

    private val _selectedCategory = MutableStateFlow("exhibition")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        fetchPrograms()
    }

    fun fetchPrograms() {
        viewModelScope.launch {
            _isLoading.value = true
            homeRepository.getProgramsByCategory(_selectedCategory.value)
                .onSuccess { _programs.value = it }
                .onFailure { Log.e("ProgramListVM", "프로그램 로드 실패: ${_selectedCategory.value}", it) }
            _isLoading.value = false
        }
    }
}
