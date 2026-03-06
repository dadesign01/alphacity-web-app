package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.repository.StampRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StampViewModel @Inject constructor(
    private val stampRepository: StampRepository,
) : ViewModel() {

    private val _stamps = MutableStateFlow<List<StampItem>>(emptyList())
    val stamps: StateFlow<List<StampItem>> = _stamps

    private val _totalStampCount = MutableStateFlow(0)
    val totalStampCount: StateFlow<Int> = _totalStampCount

    private val _userStampCount = MutableStateFlow(0)
    val userStampCount: StateFlow<Int> = _userStampCount

    private val _collectedStampIds = MutableStateFlow<Set<Int>>(emptySet())
    val collectedStampIds: StateFlow<Set<Int>> = _collectedStampIds

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchStampData() {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            val stampsDeferred = async {
                stampRepository.getStamps()
                    .onSuccess {
                        _stamps.value = it
                        _totalStampCount.value = maxOf(it.size, 1)
                    }
                    .onFailure { Log.e("StampViewModel", "스탬프 로드 실패", it) }
            }

            stampsDeferred.await()

            if (stampRepository.isLoggedIn) {
                // 유저 프로필에서 수집 수 가져오기
                stampRepository.getUserProfile()
                    .onSuccess { profile ->
                        _userStampCount.value = profile.stampCount ?: 0
                    }
                    .onFailure { Log.e("StampViewModel", "프로필 로드 실패", it) }

                // 유저가 수집한 스탬프 ID 목록
                stampRepository.getUserStamps()
                    .onSuccess { userStamps ->
                        _collectedStampIds.value = userStamps.map { it.stampId }.toSet()
                        _userStampCount.value = userStamps.size
                    }
                    .onFailure { Log.e("StampViewModel", "유저 스탬프 로드 실패", it) }
            }

            _isLoading.value = false
        }
    }
}
