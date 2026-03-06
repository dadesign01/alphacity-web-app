package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramDetailViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _isParticipated = MutableStateFlow(false)
    val isParticipated: StateFlow<Boolean> = _isParticipated

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun checkParticipation(program: ProgramItem) {
        _isParticipated.value = program.events?.any { it.isParticipated == true } == true
    }

    fun participate(program: ProgramItem) {
        if (!tokenManager.isLoggedIn) {
            _message.value = "로그인이 필요합니다"
            return
        }

        if (_isParticipated.value) {
            _message.value = "이미 참여하셨습니다"
            return
        }

        val event = program.events?.firstOrNull { it.status == "in_progress" }
        if (event == null) {
            _message.value = "참여 가능한 이벤트가 없습니다"
            return
        }

        _isLoading.value = true
        _message.value = null

        viewModelScope.launch {
            homeRepository.participateInEvent(event.id)
                .onSuccess {
                    _isParticipated.value = true
                    _message.value = "참여 완료!"
                }
                .onFailure { e ->
                    val msg = e.message ?: "참여에 실패했습니다"
                    when {
                        msg.contains("이미 참여") -> {
                            _isParticipated.value = true
                            _message.value = "이미 참여하셨습니다"
                        }
                        msg.contains("초과") -> _message.value = "참여 인원이 초과되었습니다"
                        else -> _message.value = msg
                    }
                }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
