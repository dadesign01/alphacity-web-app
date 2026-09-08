package com.alphacity.stamptour.viewmodel

import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.repository.HomeRepository
import com.alphacity.stamptour.repository.StoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MapViewModel(
    private val repository: HomeRepository = HomeRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
) {

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )

    // ------------------------------------------------------------
    // 카메라 위치 보존
    // ------------------------------------------------------------

    var savedCameraLat: Double? = null
        private set

    var savedCameraLng: Double? = null
        private set

    var savedCameraZoom: Int? = null
        private set

    fun saveCameraPosition(
        lat: Double,
        lng: Double,
        zoom: Int,
    ) {
        savedCameraLat = lat
        savedCameraLng = lng
        savedCameraZoom = zoom
    }

    val hasSavedCameraPosition: Boolean
        get() =
            savedCameraLat != null &&
                    savedCameraLng != null &&
                    savedCameraZoom != null

    // ------------------------------------------------------------
    // 원본 데이터
    // ------------------------------------------------------------

    private val _programs =
        MutableStateFlow<List<ProgramItem>>(emptyList())

    val programs: StateFlow<List<ProgramItem>>
        get() = _programs

    private val _stores =
        MutableStateFlow<List<StoreData>>(emptyList())

    val stores: StateFlow<List<StoreData>>
        get() = _stores

    private val _missions =
        MutableStateFlow<List<MissionItem>>(emptyList())

    val missions: StateFlow<List<MissionItem>>
        get() = _missions

    // ------------------------------------------------------------
    // 카테고리
    // ------------------------------------------------------------

    private val _selectedCategory =
        MutableStateFlow("all")

    val selectedCategory: StateFlow<String>
        get() = _selectedCategory

    // ------------------------------------------------------------
    // 로딩
    // ------------------------------------------------------------

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean>
        get() = _isLoading

    // ------------------------------------------------------------
    // 프로그램 필터
    // ------------------------------------------------------------

    val filteredPrograms: StateFlow<List<ProgramItem>> =
        combine(
            _programs,
            _selectedCategory,
        ) { programs, category ->

            val withCoords =
                programs.filter {
                    it.latitude != null &&
                            it.longitude != null
                }

            if (category == "all") {
                withCoords
            } else {
                withCoords.filter {
                    it.category == category
                }
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ------------------------------------------------------------
    // 매장 필터
    // ------------------------------------------------------------

    val filteredStores: StateFlow<List<StoreData>> =
        combine(
            _stores,
            _selectedCategory,
        ) { stores, category ->

            val withCoords =
                stores.filter {
                    it.latitude != null &&
                            it.longitude != null
                }

            if (
                category == "all" ||
                category == "food"
            ) {
                withCoords
            } else {
                emptyList()
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ------------------------------------------------------------
    // 미션 필터
    // ------------------------------------------------------------

    val filteredMissions: StateFlow<List<MissionItem>> =
        combine(
            _missions,
            _selectedCategory,
        ) { missions, category ->

            val withCoords =
                missions.filter {
                    it.place?.latitude != null &&
                            it.place?.longitude != null
                }

            if (
                category == "all" ||
                category == "mission"
            ) {
                withCoords
            } else {
                emptyList()
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ------------------------------------------------------------
    // 프로그램 조회
    // ------------------------------------------------------------

    fun fetchPrograms() {

        if (_programs.value.isNotEmpty()) {
            return
        }

        scope.launch {

            _isLoading.value = true

            repository
                .getPrograms()
                .onSuccess {
                    _programs.value = it
                }
                .onFailure {
                    println(
                        "[MapVM] 프로그램 로드 실패: $it"
                    )
                }

            _isLoading.value = false
        }
    }

    // ------------------------------------------------------------
    // 매장 조회
    // ------------------------------------------------------------

    fun fetchStores() {

        if (_stores.value.isNotEmpty()) {
            return
        }

        scope.launch {

            storeRepository
                .getApprovedStores()
                .onSuccess {
                    _stores.value = it
                }
                .onFailure {
                    println(
                        "[MapVM] 상점 로드 실패: $it"
                    )
                }
        }
    }

    // ------------------------------------------------------------
    // 미션 조회
    // ------------------------------------------------------------

    fun fetchMissions() {

        if (_missions.value.isNotEmpty()) {
            return
        }

        scope.launch {

            repository
                .getMissions()
                .onSuccess {
                    _missions.value = it
                }
                .onFailure {
                    println(
                        "[MapVM] 미션 로드 실패: $it"
                    )
                }
        }
    }

    // ------------------------------------------------------------
    // 카테고리 변경
    // ------------------------------------------------------------

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // ------------------------------------------------------------
    // 정리
    // ------------------------------------------------------------

    fun clear() {
        scope.coroutineContext.cancel()
    }
}