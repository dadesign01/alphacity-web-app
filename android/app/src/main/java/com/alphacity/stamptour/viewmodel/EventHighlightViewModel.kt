package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventHighlightViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<EventItem>>(emptyList())

    private val _raffleEvents = MutableStateFlow<List<EventItem>>(emptyList())
    val raffleEvents: StateFlow<List<EventItem>> = _raffleEvents

    private val _firstComeEvents = MutableStateFlow<List<EventItem>>(emptyList())
    val firstComeEvents: StateFlow<List<EventItem>> = _firstComeEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            homeRepository.getEvents()
                .onSuccess { events ->
                    _allEvents.value = events
                    _raffleEvents.value = events.filter { it.type == "raffle" }
                    _firstComeEvents.value = events.filter { it.type == "first_come" }
                }
                .onFailure { Log.e("EventHighlightVM", "이벤트 로드 실패", it) }
            _isLoading.value = false
        }
    }
}
