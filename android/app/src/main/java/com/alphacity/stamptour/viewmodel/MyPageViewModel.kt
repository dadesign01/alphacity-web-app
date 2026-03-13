package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn

    fun fetchProfile() {
        if (!tokenManager.isLoggedIn) return
        _isLoading.value = true

        viewModelScope.launch {
            homeRepository.getUserProfile()
                .onSuccess { _userProfile.value = it }
                .onFailure { Log.e("MyPageViewModel", "프로필 로드 실패", it) }
            _isLoading.value = false
        }
    }

    fun updateProfile(nickname: String, password: String?) {
        _isLoading.value = true
        _saveError.value = null
        viewModelScope.launch {
            homeRepository.updateProfile(nickname, password)
                .onSuccess { updated ->
                    _userProfile.value = updated
                    _saveSuccess.value = true
                }
                .onFailure { _saveError.value = it.message ?: "저장에 실패했습니다." }
            _isLoading.value = false
        }
    }

    fun clearSaveState() {
        _saveSuccess.value = false
        _saveError.value = null
    }

    fun logout() {
        tokenManager.clearTokens()
        _userProfile.value = null
    }
}
