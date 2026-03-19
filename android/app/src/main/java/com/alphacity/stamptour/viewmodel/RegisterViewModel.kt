package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.UserData
import com.alphacity.stamptour.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val user: UserData? = null,
    val isCodeSent: Boolean = false,
    val isPhoneVerified: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun sendCode(phone: String) {
        if (phone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "휴대폰 번호를 입력하세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.sendCode(phone)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isCodeSent = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "SMS 전송에 실패했습니다",
                    )
                }
        }
    }

    fun verifyCode(phone: String, code: String) {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "인증번호를 입력하세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.verifyCode(phone, code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isPhoneVerified = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "인증번호가 올바르지 않습니다",
                    )
                }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "이름을 입력하세요")
            return
        }
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "이메일을 입력하세요")
            return
        }
        if (!_uiState.value.isPhoneVerified) {
            _uiState.value = _uiState.value.copy(error = "휴대폰 인증을 완료하세요")
            return
        }
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = "비밀번호는 8자 이상이어야 합니다")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "비밀번호가 일치하지 않습니다")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.register(email, password, name, phone, name)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true,
                        user = data.user,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "회원가입에 실패했습니다",
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
