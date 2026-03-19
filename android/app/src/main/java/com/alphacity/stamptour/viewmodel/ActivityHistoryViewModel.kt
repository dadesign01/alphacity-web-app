package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.ActivityItemDto
import com.alphacity.stamptour.repository.ActivityHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val repository: ActivityHistoryRepository,
) : ViewModel() {

    private val _activities = MutableStateFlow<List<ActivityItemDto>>(emptyList())
    val activities: StateFlow<List<ActivityItemDto>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchActivities() {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            repository.getActivityHistory()
                .onSuccess { _activities.value = it }
                .onFailure { Log.e("ActivityHistoryVM", "활동 이력 로드 실패", it) }
            _isLoading.value = false
        }
    }
}
