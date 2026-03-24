package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ForgotPasswordStep {
    INPUT_INFO,    // 이메일 + 전화번호 입력
    VERIFY_CODE,   // 인증코드 입력
    NEW_PASSWORD,  // 새 비밀번호 설정
}

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val step: ForgotPasswordStep = ForgotPasswordStep.INPUT_INFO,
    val codeSent: Boolean = false,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var savedEmail = ""

    fun sendCode(email: String, phone: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "이메일을 입력하세요")
            return
        }
        if (phone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "전화번호를 입력하세요")
            return
        }

        savedEmail = email

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.sendVerificationCode(email, phone)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        codeSent = true,
                        step = ForgotPasswordStep.VERIFY_CODE,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "인증코드 발송에 실패했습니다",
                    )
                }
        }
    }

    fun verifyCode(code: String) {
        if (code.length != 6) {
            _uiState.value = _uiState.value.copy(error = "6자리 인증코드를 입력하세요")
            return
        }
        // 코드 검증은 비밀번호 변경 시 서버에서 수행
        _uiState.value = _uiState.value.copy(step = ForgotPasswordStep.NEW_PASSWORD)
    }

    fun resetPassword(code: String, newPassword: String, confirmPassword: String) {
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(error = "비밀번호는 8자 이상이어야 합니다")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "비밀번호가 일치하지 않습니다")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.resetPassword(savedEmail, code, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "비밀번호 변경에 실패했습니다",
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
