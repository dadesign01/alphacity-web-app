package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.StoreRegisterRequest
import com.alphacity.stamptour.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreRegisterViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitResult = MutableStateFlow<SubmitResult?>(null)
    val submitResult: StateFlow<SubmitResult?> = _submitResult

    fun registerStore(
        name: String,
        category: String,
        ownerName: String,
        phone: String,
        address: String,
        addressDetail: String,
        description: String,
        storeCode: String,
        operatingDays: String,
        openTime: String,
        closeTime: String,
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true

        viewModelScope.launch {
            val request = StoreRegisterRequest(
                name = name,
                category = category,
                ownerName = ownerName,
                phone = phone,
                address = address.ifBlank { null },
                addressDetail = addressDetail.ifBlank { null },
                description = description.ifBlank { null },
                storeCode = storeCode.ifBlank { null },
                operatingDays = operatingDays.ifBlank { null },
                openTime = openTime.ifBlank { null },
                closeTime = closeTime.ifBlank { null },
            )

            storeRepository.registerStore(request)
                .onSuccess {
                    _submitResult.value = SubmitResult.Success
                }
                .onFailure { e ->
                    Log.e("StoreRegisterVM", "등록 실패", e)
                    _submitResult.value = SubmitResult.Error(e.message ?: "등록에 실패했습니다")
                }

            _isSubmitting.value = false
        }
    }

    fun clearResult() {
        _submitResult.value = null
    }

    sealed class SubmitResult {
        data object Success : SubmitResult()
        data class Error(val message: String) : SubmitResult()
    }
}
