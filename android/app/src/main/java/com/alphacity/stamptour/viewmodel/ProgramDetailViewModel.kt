package com.alphacity.stamptour.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    private val _missions = MutableStateFlow<List<MissionItem>>(emptyList())
    val missions: StateFlow<List<MissionItem>> = _missions

    private val _isMissionsLoading = MutableStateFlow(false)
    val isMissionsLoading: StateFlow<Boolean> = _isMissionsLoading

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isNearLocation = MutableStateFlow(false)
    val isNearLocation: StateFlow<Boolean> = _isNearLocation

    private val _isCheckingLocation = MutableStateFlow(false)
    val isCheckingLocation: StateFlow<Boolean> = _isCheckingLocation

    private var tts: TextToSpeech? = null

    fun checkParticipation(program: ProgramItem) {
        // 먼저 로컬 데이터로 빠르게 표시
        _isParticipated.value = program.events?.any { it.isParticipated == true } == true

        // 서버에서 최신 데이터 조회
        if (!tokenManager.isLoggedIn) return
        viewModelScope.launch {
            homeRepository.getProgramById(program.id)
                .onSuccess { fresh ->
                    _isParticipated.value = fresh.events?.any { it.isParticipated == true } == true
                }
        }
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

    fun fetchMissions(programId: Int) {
        _isMissionsLoading.value = true
        viewModelScope.launch {
            homeRepository.getProgramMissions(programId)
                .onSuccess { _missions.value = it }
                .onFailure { _missions.value = emptyList() }
            _isMissionsLoading.value = false
        }
    }

    fun getAvailableMissions(category: String?): List<MissionItem> {
        return if (category == "seminar") {
            _missions.value.filter { it.type == "stay_time" }
        } else {
            _missions.value.filter { it.type == "quiz" || it.type == "location_auth" }
        }
    }

    fun getDisabledMissions(category: String?): List<MissionItem> {
        return if (category == "seminar") {
            _missions.value.filter { it.type != "stay_time" }
        } else {
            _missions.value.filter { it.type != "quiz" && it.type != "location_auth" }
        }
    }

    fun getDisabledMessage(category: String?): String {
        return if (category == "seminar") {
            "해당 프로그램에서는 체류시간 미션만 참여 가능합니다"
        } else {
            "해당 프로그램에서는 다른 미션을 선택해주세요"
        }
    }

    @SuppressLint("MissingPermission")
    fun checkLocationForParticipation(context: Context, targetLat: Double?, targetLng: Double?) {
        if (targetLat == null || targetLng == null) {
            _isNearLocation.value = true
            return
        }
        _isCheckingLocation.value = true
        viewModelScope.launch {
            val location = getCurrentLocation(context)
            _isNearLocation.value = if (location == null) {
                true // 위치 확인 불가 시 참여 허용
            } else {
                val target = Location("target").apply {
                    latitude = targetLat
                    longitude = targetLng
                }
                location.distanceTo(target) <= 100f
            }
            _isCheckingLocation.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? {
        return suspendCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
                    .addOnSuccessListener { location -> cont.resume(location) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (_: Exception) {
                cont.resume(null)
            }
        }
    }

    fun initTts(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                    override fun onError(utteranceId: String?) { _isSpeaking.value = false }
                })
            }
        }
    }

    fun speakDescription(text: String) {
        if (_isSpeaking.value) {
            stopSpeaking()
        } else {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "program_description")
            _isSpeaking.value = true
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
