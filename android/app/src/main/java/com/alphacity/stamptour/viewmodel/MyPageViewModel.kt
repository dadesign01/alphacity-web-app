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

    fun logout() {
        tokenManager.clearTokens()
        _userProfile.value = null
    }
}
