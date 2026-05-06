package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.FestivalSelectionRepository
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramListViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val festivalSelection: FestivalSelectionRepository,
) : ViewModel() {

    private val _programs = MutableStateFlow<List<ProgramItem>>(emptyList())
    val programs: StateFlow<List<ProgramItem>> = _programs

    private val _selectedCategory = MutableStateFlow("exhibition")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _festivals = MutableStateFlow<List<FestivalItem>>(emptyList())
    val festivals: StateFlow<List<FestivalItem>> = _festivals

    val selectedFestivalId: StateFlow<Int?> = festivalSelection.selectedFestivalId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            homeRepository.getFestivals()
                .onSuccess { _festivals.value = it }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        fetchPrograms()
    }

    fun selectFestival(id: Int?) {
        festivalSelection.select(id)
        fetchPrograms()
    }

    fun fetchPrograms() {
        viewModelScope.launch {
            _isLoading.value = true
            homeRepository.getPrograms(festivalId = festivalSelection.selectedFestivalId.value, category = _selectedCategory.value)
                .onSuccess { _programs.value = it }
                .onFailure { Log.e("ProgramListVM", "프로그램 로드 실패: ${_selectedCategory.value}", it) }
            _isLoading.value = false
        }
    }
}
