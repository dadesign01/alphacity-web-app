package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.FestivalDetail
import com.alphacity.stamptour.repository.FestivalSelectionRepository
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FestivalDetailViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val festivalSelection: FestivalSelectionRepository,
) : ViewModel() {

    private val _detail = MutableStateFlow<FestivalDetail?>(null)
    val detail: StateFlow<FestivalDetail?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(festivalId: Int) {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            homeRepository.getFestivalDetail(festivalId)
                .onSuccess { _detail.value = it }
            _isLoading.value = false
        }
    }

    fun selectThisFestival(festivalId: Int) {
        festivalSelection.select(festivalId)
    }
}
