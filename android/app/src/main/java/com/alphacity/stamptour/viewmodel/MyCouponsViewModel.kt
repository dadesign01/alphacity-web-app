package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.repository.StampRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCouponsViewModel @Inject constructor(
    private val stampRepository: StampRepository,
) : ViewModel() {

    private val _myCoupons = MutableStateFlow<List<MyCouponItem>>(emptyList())
    val myCoupons: StateFlow<List<MyCouponItem>> = _myCoupons

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _useSuccess = MutableStateFlow(false)
    val useSuccess: StateFlow<Boolean> = _useSuccess

    private val _useError = MutableStateFlow<String?>(null)
    val useError: StateFlow<String?> = _useError

    fun useCoupon(userCouponId: Int) {
        viewModelScope.launch {
            stampRepository.useCoupon(userCouponId)
                .onSuccess {
                    _useSuccess.value = true
                    fetchMyCoupons()
                }
                .onFailure {
                    _useError.value = it.message ?: "쿠폰 사용에 실패했습니다"
                }
        }
    }

    fun clearUseSuccess() { _useSuccess.value = false }
    fun clearUseError() { _useError.value = null }

    fun fetchMyCoupons() {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            stampRepository.getMyCoupons()
                .onSuccess { _myCoupons.value = it }
                .onFailure { Log.e("MyCouponsVM", "내 쿠폰 로드 실패", it) }
            _isLoading.value = false
        }
    }
}
