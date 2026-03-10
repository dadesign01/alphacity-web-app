package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.EventParticipationResult
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _event = MutableStateFlow<EventItem?>(null)
    val event: StateFlow<EventItem?> = _event

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isParticipating = MutableStateFlow(false)
    val isParticipating: StateFlow<Boolean> = _isParticipating

    private val _myRaffleNumber = MutableStateFlow<Int?>(null)
    val myRaffleNumber: StateFlow<Int?> = _myRaffleNumber

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun fetchEventDetail(eventId: Int) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            homeRepository.getEventDetail(eventId)
                .onSuccess { data ->
                    _event.value = data
                    if (data.isParticipated == true) {
                        _myRaffleNumber.value = data.myRaffleNumber
                    }
                }
                .onFailure {
                    _message.value = "이벤트 정보를 불러올 수 없습니다"
                }
            _isLoading.value = false
        }
    }

    fun participate(eventId: Int) {
        if (_isParticipating.value) return

        if (!tokenManager.isLoggedIn) {
            _message.value = "로그인이 필요합니다"
            return
        }

        if (_event.value?.isParticipated == true || _myRaffleNumber.value != null) {
            _message.value = "이미 참여하셨습니다"
            return
        }

        _isParticipating.value = true

        viewModelScope.launch {
            homeRepository.participateInEvent(eventId)
                .onSuccess { result ->
                    _myRaffleNumber.value = result.raffleNumber
                    _event.value?.let { current ->
                        _event.value = current.copy(
                            participantCount = current.participantCount + 1,
                            isParticipated = true,
                            myRaffleNumber = result.raffleNumber,
                        )
                    }
                    _message.value = "참여 완료!"
                }
                .onFailure { e ->
                    _message.value = e.message ?: "참여에 실패했습니다"
                }
            _isParticipating.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
