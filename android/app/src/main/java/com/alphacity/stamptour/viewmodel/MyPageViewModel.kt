package com.alphacity.stamptour.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.repository.AuthRepository
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository,
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

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError

    // SMS 인증 상태
    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent: StateFlow<Boolean> = _isCodeSent

    private val _isPhoneVerified = MutableStateFlow(false)
    val isPhoneVerified: StateFlow<Boolean> = _isPhoneVerified

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError

    // 로컬에서 선택한 프로필 이미지 URI (앱 세션 동안 유지)
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

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

    fun sendCode(phone: String) {
        if (phone.isBlank()) {
            _verificationError.value = "휴대폰 번호를 입력하세요"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _verificationError.value = null
            authRepository.sendCode(phone)
                .onSuccess {
                    _isCodeSent.value = true
                }
                .onFailure { e ->
                    _verificationError.value = e.message ?: "SMS 전송에 실패했습니다"
                }
            _isLoading.value = false
        }
    }

    fun verifyCode(phone: String, code: String) {
        if (code.isBlank()) {
            _verificationError.value = "인증번호를 입력하세요"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _verificationError.value = null
            authRepository.verifyCode(phone, code)
                .onSuccess {
                    _isPhoneVerified.value = true
                }
                .onFailure { e ->
                    _verificationError.value = e.message ?: "인증번호가 올바르지 않습니다"
                }
            _isLoading.value = false
        }
    }

    fun setPhoneVerifiedFromProfile() {
        _isPhoneVerified.value = true
    }

    fun resetVerificationState() {
        _isCodeSent.value = false
        _isPhoneVerified.value = false
        _verificationError.value = null
    }

    fun updateProfile(nickname: String, currentPassword: String?, newPassword: String?, phone: String? = null, name: String? = null, address: String? = null, addressDetail: String? = null, birthDate: String? = null, gender: String? = null) {
        _isLoading.value = true
        _saveError.value = null
        viewModelScope.launch {
            homeRepository.updateProfile(nickname, currentPassword, newPassword, phone, name, address, addressDetail, birthDate, gender)
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

    fun deleteAccount() {
        _isLoading.value = true
        _deleteError.value = null
        viewModelScope.launch {
            homeRepository.deleteAccount()
                .onSuccess {
                    tokenManager.clearTokens()
                    _userProfile.value = null
                    _deleteSuccess.value = true
                }
                .onFailure {
                    _deleteError.value = it.message ?: "회원 탈퇴에 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    fun clearDeleteState() {
        _deleteSuccess.value = false
        _deleteError.value = null
    }

    fun logout() {
        tokenManager.clearTokens()
        _userProfile.value = null
    }
}
