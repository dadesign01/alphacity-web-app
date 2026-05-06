package com.alphacity.stamptour.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전역 "선택된 축제" 상태.
 * - null = 전체 축제 (필터 없음)
 * - 특정 ID = 해당 축제로만 필터링 (스탬프/쿠폰/이벤트/프로그램 등)
 */
@Singleton
class FestivalSelectionRepository @Inject constructor() {
    private val _selectedFestivalId = MutableStateFlow<Int?>(null)
    val selectedFestivalId: StateFlow<Int?> = _selectedFestivalId.asStateFlow()

    fun select(id: Int?) {
        _selectedFestivalId.value = id
    }
}
