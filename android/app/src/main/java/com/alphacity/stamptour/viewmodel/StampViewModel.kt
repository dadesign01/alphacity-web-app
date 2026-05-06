package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.CouponItem
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem
import com.alphacity.stamptour.repository.FestivalSelectionRepository
import com.alphacity.stamptour.repository.HomeRepository
import com.alphacity.stamptour.repository.StampRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RedeemSuccess(
    val couponName: String,
    val couponDescription: String?,
    val validUntil: String,
    val requiredStamps: Int,
)

@HiltViewModel
class StampViewModel @Inject constructor(
    private val stampRepository: StampRepository,
    private val homeRepository: HomeRepository,
    private val festivalSelection: FestivalSelectionRepository,
) : ViewModel() {

    private val _festivals = MutableStateFlow<List<FestivalItem>>(emptyList())
    val festivals: StateFlow<List<FestivalItem>> = _festivals

    val selectedFestivalId: StateFlow<Int?> = festivalSelection.selectedFestivalId

    fun selectFestival(id: Int?) {
        festivalSelection.select(id)
        fetchStampData()
    }

    private val _stamps = MutableStateFlow<List<StampItem>>(emptyList())
    val stamps: StateFlow<List<StampItem>> = _stamps

    private val _totalStampCount = MutableStateFlow(0)
    val totalStampCount: StateFlow<Int> = _totalStampCount

    private val _userStampCount = MutableStateFlow(0)
    val userStampCount: StateFlow<Int> = _userStampCount

    private val _collectedStampIds = MutableStateFlow<Set<Int>>(emptySet())
    val collectedStampIds: StateFlow<Set<Int>> = _collectedStampIds

    private val _userStamps = MutableStateFlow<List<UserStampItem>>(emptyList())
    val userStamps: StateFlow<List<UserStampItem>> = _userStamps

    private val _missions = MutableStateFlow<List<MissionItem>>(emptyList())
    val missions: StateFlow<List<MissionItem>> = _missions

    private val _coupons = MutableStateFlow<List<CouponItem>>(emptyList())
    val coupons: StateFlow<List<CouponItem>> = _coupons

    private val _redeemedCouponIds = MutableStateFlow<Set<Int>>(emptySet())
    val redeemedCouponIds: StateFlow<Set<Int>> = _redeemedCouponIds

    private val _isRedeeming = MutableStateFlow(false)
    val isRedeeming: StateFlow<Boolean> = _isRedeeming

    private val _redeemSuccess = MutableStateFlow<RedeemSuccess?>(null)
    val redeemSuccess: StateFlow<RedeemSuccess?> = _redeemSuccess

    private val _redeemError = MutableStateFlow<String?>(null)
    val redeemError: StateFlow<String?> = _redeemError

    private val _availableStamps = MutableStateFlow(0)
    val availableStamps: StateFlow<Int> = _availableStamps

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchStampData() {
        if (_isLoading.value) return
        _isLoading.value = true

        val festivalId = festivalSelection.selectedFestivalId.value

        viewModelScope.launch {
            // 축제 목록 (드롭다운용)
            if (_festivals.value.isEmpty()) {
                homeRepository.getFestivals()
                    .onSuccess { _festivals.value = it }
                    .onFailure { Log.e("StampViewModel", "축제 로드 실패", it) }
            }

            val stampsDeferred = async {
                stampRepository.getStamps(festivalId)
                    .onSuccess {
                        _stamps.value = it
                        _totalStampCount.value = maxOf(it.size, 1)
                    }
                    .onFailure { Log.e("StampViewModel", "스탬프 로드 실패", it) }
            }

            val missionsDeferred = async {
                stampRepository.getMissions(festivalId)
                    .onSuccess { _missions.value = it }
                    .onFailure { Log.e("StampViewModel", "미션 로드 실패", it) }
            }

            stampsDeferred.await()
            missionsDeferred.await()

            if (stampRepository.isLoggedIn) {
                // 유저가 수집한 스탬프 (축제별 필터링)
                stampRepository.getUserStamps(festivalId)
                    .onSuccess { userStampsList ->
                        _userStamps.value = userStampsList
                        _collectedStampIds.value = userStampsList.map { it.stampId }.toSet()
                        _userStampCount.value = userStampsList.size
                    }
                    .onFailure { Log.e("StampViewModel", "유저 스탬프 로드 실패", it) }
            }

            // 쿠폰 목록 로드 (축제별)
            stampRepository.getCoupons(festivalId)
                .onSuccess {
                    _coupons.value = it.coupons
                    _redeemedCouponIds.value = it.redeemedCouponIds.toSet()
                    _availableStamps.value = it.availableStamps
                }
                .onFailure { Log.e("StampViewModel", "쿠폰 로드 실패", it) }

            _isLoading.value = false
        }
    }

    fun redeemCoupon(couponId: Int) {
        if (_isRedeeming.value) return
        _isRedeeming.value = true
        _redeemSuccess.value = null
        _redeemError.value = null

        val coupon = _coupons.value.find { it.id == couponId }

        viewModelScope.launch {
            stampRepository.redeemCoupon(couponId)
                .onSuccess {
                    _redeemSuccess.value = RedeemSuccess(
                        couponName = coupon?.name ?: "쿠폰",
                        couponDescription = coupon?.description,
                        validUntil = coupon?.validUntil ?: "",
                        requiredStamps = coupon?.requiredStamps ?: 0,
                    )
                    fetchStampData()
                }
                .onFailure {
                    _redeemError.value = it.message ?: "쿠폰 교환에 실패했습니다"
                }
            _isRedeeming.value = false
        }
    }

    fun clearRedeemSuccess() {
        _redeemSuccess.value = null
    }

    fun clearRedeemError() {
        _redeemError.value = null
    }
}
