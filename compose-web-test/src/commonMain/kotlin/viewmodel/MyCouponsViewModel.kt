package com.alphacity.stamptour.viewmodel

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MyCouponItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyCouponsViewModel {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _myCoupons = MutableStateFlow<List<MyCouponItem>>(emptyList())
    val myCoupons: StateFlow<List<MyCouponItem>> = _myCoupons

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _useSuccess = MutableStateFlow(false)
    val useSuccess: StateFlow<Boolean> = _useSuccess

    private val _useError = MutableStateFlow<String?>(null)
    val useError: StateFlow<String?> = _useError

    fun fetchMyCoupons() {
        if (_isLoading.value) return

        _isLoading.value = true
        _useError.value = null

        scope.launch {
            try {
                val response = ApiService.getMyCoupons()

                if (response.success && response.data != null) {
                    _myCoupons.value = response.data
                } else {
                    _useError.value =
                        response.error?.message ?: "내 쿠폰 로드 실패"
                }
            } catch (e: Exception) {
                _useError.value =
                    e.message ?: "내 쿠폰 로드 실패"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun useCoupon(userCouponId: Int) {
        _useError.value = null

        scope.launch {
            try {
                val response = ApiService.useCoupon(userCouponId)

                if (response.success && response.data != null) {
                    _useSuccess.value = true

                    // 쿠폰 사용 후 최신 쿠폰 목록 다시 조회
                    fetchMyCoupons()
                } else {
                    _useError.value =
                        response.error?.message ?: "쿠폰 사용에 실패했습니다"
                }
            } catch (e: Exception) {
                _useError.value =
                    e.message ?: "쿠폰 사용에 실패했습니다"
            }
        }
    }

    fun clearUseSuccess() {
        _useSuccess.value = false
    }

    fun clearUseError() {
        _useError.value = null
    }
}
