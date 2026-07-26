package com.alphacity.stamptour.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.repository.HomeRepository
import com.alphacity.stamptour.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    // 카메라 위치 보존 (네비게이션 복귀 시 복원용)
    var savedCameraLat: Double? = null
        private set
    var savedCameraLng: Double? = null
        private set
    var savedCameraZoom: Int? = null
        private set

    fun saveCameraPosition(lat: Double, lng: Double, zoom: Int) {
        savedCameraLat = lat
        savedCameraLng = lng
        savedCameraZoom = zoom
    }

    val hasSavedCameraPosition: Boolean
        get() = savedCameraLat != null && savedCameraLng != null && savedCameraZoom != null

    private val _programs = MutableStateFlow<List<ProgramItem>>(emptyList())

    private val _stores = MutableStateFlow<List<StoreData>>(emptyList())
    val stores: StateFlow<List<StoreData>> = _stores

    private val _missions = MutableStateFlow<List<MissionItem>>(emptyList())

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val filteredPrograms: StateFlow<List<ProgramItem>> = combine(
        _programs, _selectedCategory
    ) { programs, category ->
        val withCoords = programs.filter { it.latitude != null && it.longitude != null }
        if (category == "all") withCoords
        else withCoords.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStores: StateFlow<List<StoreData>> = combine(
        _stores, _selectedCategory
    ) { stores, category ->
        val withCoords = stores.filter { it.latitude != null && it.longitude != null }
        if (category == "all" || category == "food") withCoords
        else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 좌표가 있는 미션만 지도에 표시 (quiz 미션은 place 좌표가 없어 제외)
    val filteredMissions: StateFlow<List<MissionItem>> = combine(
        _missions, _selectedCategory
    ) { missions, category ->
        val withCoords = missions.filter { it.place?.latitude != null && it.place?.longitude != null }
        if (category == "all" || category == "mission") withCoords
        else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchPrograms() {
        if (_isLoading.value) return
        if (_programs.value.isNotEmpty()) return
        _isLoading.value = true

        viewModelScope.launch {
            repository.getPrograms()
                .onSuccess { _programs.value = it }
                .onFailure { println("[MapVM] 프로그램 로드 실패: $it") }
            _isLoading.value = false
        }
    }

    fun fetchStores() {
        if (_stores.value.isNotEmpty()) return

        viewModelScope.launch {
            storeRepository.getApprovedStores()
                .onSuccess { _stores.value = it }
                .onFailure { println("[MapVM] 상점 로드 실패: $it") }
        }
    }

    fun fetchMissions() {
        if (_missions.value.isNotEmpty()) return

        viewModelScope.launch {
            repository.getMissions()
                .onSuccess { _missions.value = it }
                .onFailure { println("[MapVM] 미션 로드 실패: $it") }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
