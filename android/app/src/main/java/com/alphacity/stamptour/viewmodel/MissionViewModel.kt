package com.alphacity.stamptour.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.repository.HomeRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _showAlert = MutableStateFlow(false)
    val showAlert: StateFlow<Boolean> = _showAlert

    private val _alertMessage = MutableStateFlow("")
    val alertMessage: StateFlow<String> = _alertMessage

    private val _earnedStamp = MutableStateFlow<StampItem?>(null)
    val earnedStamp: StateFlow<StampItem?> = _earnedStamp

    // 체류시간 미션
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds

    private var timerJob: Job? = null

    // MARK: - 퀴즈 미션 완료

    fun completeQuizMission(missionId: Int, answer: String) {
        if (_isLoading.value) return
        if (answer.isBlank()) {
            _alertMessage.value = "답변을 입력해주세요"
            _showAlert.value = true
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            homeRepository.completeMission(missionId, answer)
                .onSuccess { result ->
                    _earnedStamp.value = result.stamp
                    _isCompleted.value = true
                    _message.value = "정답입니다! 미션 완료!"
                }
                .onFailure { e ->
                    val msg = e.message ?: "미션 완료에 실패했습니다"
                    when {
                        msg.contains("정답") -> _alertMessage.value = "정답이 아닙니다"
                        msg.contains("이미 완료") -> {
                            _isCompleted.value = true
                            _alertMessage.value = "이미 완료한 미션입니다"
                        }
                        else -> _alertMessage.value = msg
                    }
                    _showAlert.value = true
                }
            _isLoading.value = false
        }
    }

    // MARK: - 위치 인증 미션 완료

    @SuppressLint("MissingPermission")
    fun completeLocationMission(context: Context, missionId: Int, targetLat: Double, targetLng: Double) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            val location = getCurrentLocation(context)

            if (location == null) {
                _alertMessage.value = "위치를 가져올 수 없습니다. 위치 권한을 확인해주세요."
                _showAlert.value = true
                _isLoading.value = false
                return@launch
            }

            val target = Location("target").apply {
                latitude = targetLat
                longitude = targetLng
            }
            val distance = location.distanceTo(target)

            if (distance <= 100f) {
                homeRepository.completeMission(missionId)
                    .onSuccess { result ->
                        _earnedStamp.value = result.stamp
                        _isCompleted.value = true
                        _message.value = "위치 인증 완료!"
                    }
                    .onFailure { e ->
                        val msg = e.message ?: "미션 완료에 실패했습니다"
                        if (msg.contains("이미 완료")) {
                            _isCompleted.value = true
                            _alertMessage.value = "이미 완료한 미션입니다"
                        } else {
                            _alertMessage.value = msg
                        }
                        _showAlert.value = true
                    }
            } else {
                _alertMessage.value = "해당 장소 근처에서 인증해주세요.\n(현재 거리: ${distance.toInt()}m)"
                _showAlert.value = true
            }
            _isLoading.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? {
        return suspendCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
                    .addOnSuccessListener { location ->
                        cont.resume(location)
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            } catch (_: Exception) {
                cont.resume(null)
            }
        }
    }

    // MARK: - 체류시간 미션

    fun startStayTimeMission(missionId: Int, minutes: Int) {
        if (_isTimerRunning.value) return
        _remainingSeconds.value = minutes * 60
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            _isTimerRunning.value = false
            completeStayTimeMission(missionId)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
    }

    private suspend fun completeStayTimeMission(missionId: Int) {
        _isLoading.value = true
        homeRepository.completeMission(missionId)
            .onSuccess { result ->
                _earnedStamp.value = result.stamp
                _isCompleted.value = true
                _message.value = "체류시간 미션 완료!"
            }
            .onFailure { e ->
                val msg = e.message ?: "미션 완료에 실패했습니다"
                if (msg.contains("이미 완료")) {
                    _isCompleted.value = true
                    _alertMessage.value = "이미 완료한 미션입니다"
                } else {
                    _alertMessage.value = msg
                }
                _showAlert.value = true
            }
        _isLoading.value = false
    }

    fun dismissAlert() {
        _showAlert.value = false
    }

    fun clearMessage() {
        _message.value = null
    }

    val formattedTime: String
        get() {
            val mins = _remainingSeconds.value / 60
            val secs = _remainingSeconds.value % 60
            return String.format("%02d:%02d", mins, secs)
        }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
