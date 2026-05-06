package com.alphacity.stamptour.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.FestivalSelectionRepository
import com.alphacity.stamptour.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val festivalSelection: FestivalSelectionRepository,
) : ViewModel() {

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners

    private val _festivals = MutableStateFlow<List<FestivalItem>>(emptyList())
    val festivals: StateFlow<List<FestivalItem>> = _festivals

    val selectedFestivalId: StateFlow<Int?> = festivalSelection.selectedFestivalId

    fun selectFestival(id: Int?) = festivalSelection.select(id)

    private val _programs = MutableStateFlow<List<ProgramItem>>(emptyList())
    val programs: StateFlow<List<ProgramItem>> = _programs

    private val _events = MutableStateFlow<List<EventItem>>(emptyList())
    val events: StateFlow<List<EventItem>> = _events

    private val _totalStampCount = MutableStateFlow(10)
    val totalStampCount: StateFlow<Int> = _totalStampCount

    private val _userStampCount = MutableStateFlow(0)
    val userStampCount: StateFlow<Int> = _userStampCount

    private val _userCouponCount = MutableStateFlow(0)
    val userCouponCount: StateFlow<Int> = _userCouponCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchHomeData() {
        if (_isLoading.value) return
        _isLoading.value = true

        val festivalId = festivalSelection.selectedFestivalId.value

        viewModelScope.launch {
            val bannersDeferred = async {
                homeRepository.getBanners()
                    .onSuccess { _banners.value = it }
                    .onFailure { Log.e("HomeViewModel", "배너 로드 실패", it) }
            }

            val festivalsDeferred = async {
                homeRepository.getFestivals()
                    .onSuccess { _festivals.value = it }
                    .onFailure { Log.e("HomeViewModel", "축제 로드 실패", it) }
            }

            val programsDeferred = async {
                homeRepository.getPrograms(festivalId = festivalId)
                    .onSuccess { programs ->
                        _programs.value = filterTodayPrograms(programs)
                    }
                    .onFailure { Log.e("HomeViewModel", "프로그램 로드 실패", it) }
            }

            val eventsDeferred = async {
                homeRepository.getEvents(festivalId = festivalId)
                    .onSuccess { _events.value = it }
                    .onFailure { Log.e("HomeViewModel", "이벤트 로드 실패", it) }
            }

            val stampsDeferred = async {
                homeRepository.getStamps(festivalId = festivalId)
                    .onSuccess { _totalStampCount.value = maxOf(it.size, 1) }
                    .onFailure { Log.e("HomeViewModel", "스탬프 로드 실패", it) }
            }

            bannersDeferred.await()
            festivalsDeferred.await()
            programsDeferred.await()
            eventsDeferred.await()
            stampsDeferred.await()

            // 로그인 상태면 유저 프로필도 가져오기
            if (homeRepository.isLoggedIn) {
                homeRepository.getUserProfile()
                    .onSuccess { profile ->
                        _userStampCount.value = profile.stampCount ?: 0
                        _userCouponCount.value = profile.couponCount ?: 0
                    }
                    .onFailure { Log.e("HomeViewModel", "프로필 로드 실패", it) }
            }

            _isLoading.value = false
        }
    }

    private fun filterTodayPrograms(programs: List<ProgramItem>): List<ProgramItem> {
        val today = Date()
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return programs.filter { program ->
            if (program.status == "in_progress") return@filter true
            try {
                val start = parser.parse(program.startDate.take(10))
                val end = parser.parse(program.endDate.take(10))
                start != null && end != null && !today.before(start) && !today.after(end)
            } catch (_: Exception) {
                true // 파싱 실패 시 포함
            }
        }.ifEmpty { programs } // 필터 결과 없으면 전체 표시
    }
}
