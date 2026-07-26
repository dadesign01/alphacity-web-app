package com.alphacity.stamptour.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import android.os.Handler
import android.os.Looper
import kotlin.math.floor

private data class MapCluster(
    val centerLat: Double,
    val centerLng: Double,
    val programs: List<ProgramItem>,
) {
    val count get() = programs.size
    val isSingle get() = programs.size == 1
}

private fun clusterPrograms(programs: List<ProgramItem>, zoomLevel: Int): List<MapCluster> {
    val valid = programs.filter { it.latitude != null && it.longitude != null }
    if (valid.isEmpty()) return emptyList()

    if (zoomLevel >= 18) {
        return valid.map { MapCluster(it.latitude!!, it.longitude!!, listOf(it)) }
    }

    val gridSize = gridSizeFor(zoomLevel)

    val grid = mutableMapOf<String, MutableList<ProgramItem>>()
    for (p in valid) {
        val key = "${floor(p.latitude!! / gridSize).toInt()}_${floor(p.longitude!! / gridSize).toInt()}"
        grid.getOrPut(key) { mutableListOf() }.add(p)
    }

    return grid.values.map { items ->
        val avgLat = items.mapNotNull { it.latitude }.average()
        val avgLng = items.mapNotNull { it.longitude }.average()
        MapCluster(avgLat, avgLng, items)
    }
}

private fun gridSizeFor(zoomLevel: Int): Double = when (zoomLevel) {
    in 0..10 -> 0.05
    in 11..12 -> 0.02
    13 -> 0.01
    14 -> 0.005
    15 -> 0.003
    16 -> 0.0015
    else -> 0.0008
}

private data class MissionCluster(
    val centerLat: Double,
    val centerLng: Double,
    val missions: List<MissionItem>,
) {
    val count get() = missions.size
    val isSingle get() = missions.size == 1
}

private fun clusterMissions(missions: List<MissionItem>, zoomLevel: Int): List<MissionCluster> {
    val valid = missions.filter { it.place?.latitude != null && it.place?.longitude != null }
    if (valid.isEmpty()) return emptyList()

    if (zoomLevel >= 18) {
        return valid.map { MissionCluster(it.place!!.latitude!!, it.place!!.longitude!!, listOf(it)) }
    }

    val gridSize = gridSizeFor(zoomLevel)
    val grid = mutableMapOf<String, MutableList<MissionItem>>()
    for (m in valid) {
        val lat = m.place!!.latitude!!
        val lng = m.place!!.longitude!!
        val key = "${floor(lat / gridSize).toInt()}_${floor(lng / gridSize).toInt()}"
        grid.getOrPut(key) { mutableListOf() }.add(m)
    }

    return grid.values.map { items ->
        val avgLat = items.mapNotNull { it.place?.latitude }.average()
        val avgLng = items.mapNotNull { it.place?.longitude }.average()
        MissionCluster(avgLat, avgLng, items)
    }
}

// 클러스터 탭 시 실제로 클러스터가 풀리는 최소 줌 (해제 기준인 18을 넘지 않음)
private fun splitZoomForPrograms(programs: List<ProgramItem>, currentZoom: Int): Int {
    for (z in (currentZoom + 1)..18) {
        if (clusterPrograms(programs, z).size > 1) return z
    }
    return 18
}

private fun splitZoomForMissions(missions: List<MissionItem>, currentZoom: Int): Int {
    for (z in (currentZoom + 1)..18) {
        if (clusterMissions(missions, z).size > 1) return z
    }
    return 18
}

private data class MapCategory(val key: String, val label: String)

private val categories = listOf(
    MapCategory("all", "전체"),
    MapCategory("mission", "미션"),
    MapCategory("food", "맛집"),
    MapCategory("exhibition", "전시"),
    MapCategory("seminar", "세미나"),
    MapCategory("event", "이벤트"),
)

@Composable
fun MapScreen(
    onProgramClick: (ProgramItem) -> Unit = {},
    onStoreClick: (StoreData) -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
    focusLat: Double? = null,
    focusLng: Double? = null,
    onFocusConsumed: () -> Unit = {},
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    val filteredStores by viewModel.filteredStores.collectAsState()
    val filteredMissions by viewModel.filteredMissions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPrograms()
        viewModel.fetchStores()
        viewModel.fetchMissions()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        MapHeader(onProfileClick = onNavigateToMyPage, showBack = showBack, onBack = onBack)

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            KakaoMapContent(
                programs = filteredPrograms,
                stores = filteredStores,
                missions = filteredMissions,
                onProgramClick = { program ->
                    onProgramClick(program)
                },
                onStoreClick = { store ->
                    onStoreClick(store)
                },
                focusLat = focusLat,
                focusLng = focusLng,
                onFocusConsumed = onFocusConsumed,
                viewModel = viewModel,
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 15.dp, start = 20.dp, end = 20.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat.key
                    Surface(
                        onClick = { viewModel.selectCategory(cat.key) },
                        shape = RoundedCornerShape(25.dp),
                        color = if (isSelected) Primary else Color(0xFFFBFCFF),
                        border = if (isSelected) null else BorderStroke(1.dp, Primary),
                    ) {
                        Text(
                            text = cat.label,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapHeader(
    onProfileClick: () -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            // 축제/행사 상세에서 '지도에서 보기'로 진입: 로고 대신 뒤로가기 + 제목
            Image(
                painter = painterResource(id = R.drawable.icon_back_arrow),
                contentDescription = "뒤로",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(13.dp, 26.dp)
                    .clickable { onBack() },
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "지도 상세보기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                letterSpacing = (-0.36).sp,
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            // 하단 탭 '지도'로 진입: 로고 + 프로필
            Image(
                painter = painterResource(id = R.drawable.header_logo),
                contentDescription = "로고",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "올리모아",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                letterSpacing = (-0.36).sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.icon_profile),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFEBEBEB), CircleShape)
                    .clickable { onProfileClick() },
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun KakaoMapContent(
    programs: List<ProgramItem>,
    stores: List<StoreData> = emptyList(),
    missions: List<MissionItem> = emptyList(),
    onProgramClick: (ProgramItem) -> Unit,
    onStoreClick: (StoreData) -> Unit = {},
    focusLat: Double? = null,
    focusLng: Double? = null,
    onFocusConsumed: () -> Unit = {},
    viewModel: MapViewModel? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }

    // 맵 상태 holder - remember object로 리컴포지션에도 유지됨
    val mapState = remember {
        object {
            var kakaoMap: KakaoMap? = null
            var zoomLevel: Int = 15
        }
    }

    // onMapReady는 비동기로 나중에 호출되므로, 콜백 클로저가 최신 focus 좌표를 보도록 유지
    val latestFocusLat by rememberUpdatedState(focusLat)
    val latestFocusLng by rememberUpdatedState(focusLng)
    val latestOnFocusConsumed by rememberUpdatedState(onFocusConsumed)
    val currentClusters = remember { mutableListOf<MapCluster>() }
    val currentMissionClusters = remember { mutableListOf<MissionCluster>() }

    // 최신 programs를 ref로 유지 (zoom 체커 클로저 stale 방지)
    val programsRef = remember { mutableListOf<ProgramItem>() }
    val storesRef = remember { mutableListOf<StoreData>() }
    val missionsRef = remember { mutableListOf<MissionItem>() }

    // 현위치 마커용 상태
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    // 열면 현재 위치 중심으로 자동 이동했는지 (최초 1회만 / focus 진입 시엔 생략)
    var didCenterOnUser by remember { mutableStateOf(false) }
    // 포커스/GPS/저장 카메라가 없을 때 데이터 위치로 1회 자동 센터링했는지
    var didCenterOnData by remember { mutableStateOf(false) }

    // 위치 권한 요청
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLat = loc.latitude
                        userLng = loc.longitude
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    // 현위치 취득
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLat = loc.latitude
                        userLng = loc.longitude
                    }
                }
            } catch (_: SecurityException) {}
        } else {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    // programs가 바뀔 때마다 ref 동기화 + 마커 갱신
    LaunchedEffect(programs) {
        programsRef.clear()
        programsRef.addAll(programs)
        mapState.kakaoMap?.let { map ->
            currentClusters.clear()
            currentClusters.addAll(
                addClusteredMarkers(map, programs, mapState.zoomLevel, context)
            )

            // 포커스/GPS/저장 카메라가 없으면 기본 좌표 대신 등록된 프로그램 위치로 1회 이동
            if (!didCenterOnData && !didCenterOnUser && focusLat == null && focusLng == null &&
                userLat == null && viewModel?.hasSavedCameraPosition != true
            ) {
                val valid = programs.filter { it.latitude != null && it.longitude != null }
                if (valid.isNotEmpty()) {
                    val avgLat = valid.mapNotNull { it.latitude }.average()
                    val avgLng = valid.mapNotNull { it.longitude }.average()
                    mapState.zoomLevel = 15
                    map.moveCamera(
                        CameraUpdateFactory.newCenterPosition(LatLng.from(avgLat, avgLng), 15),
                        CameraAnimation.from(300),
                    )
                    didCenterOnData = true
                }
            }
        }
    }

    // stores가 바뀔 때마다 ref 동기화 + 마커 갱신
    LaunchedEffect(stores) {
        storesRef.clear()
        storesRef.addAll(stores)
        mapState.kakaoMap?.let { map ->
            addStoreMarkers(map, stores, context)
        }
    }

    // missions가 바뀔 때마다 ref 동기화 + 마커 갱신
    LaunchedEffect(missions) {
        missionsRef.clear()
        missionsRef.addAll(missions)
        mapState.kakaoMap?.let { map ->
            currentMissionClusters.clear()
            currentMissionClusters.addAll(
                addClusteredMissionMarkers(map, missions, mapState.zoomLevel, context)
            )
        }
    }

    // 현위치 마커 갱신 + (열면 현재 위치 중심) 최초 1회 카메라 이동
    LaunchedEffect(userLat, userLng) {
        val lat = userLat ?: return@LaunchedEffect
        val lng = userLng ?: return@LaunchedEffect
        mapState.kakaoMap?.let { map ->
            updateLocationMarker(map, lat, lng, context)
            if (!didCenterOnUser && focusLat == null && focusLng == null) {
                mapState.zoomLevel = 16
                map.moveCamera(
                    CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), 16),
                    CameraAnimation.from(500),
                )
                didCenterOnUser = true
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(error: Exception?) {
                            println("[MapScreen] 카카오맵 에러: ${error?.message}")
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            mapState.kakaoMap = map

                            // 카카오 기본 POI(식당/상호명 등) 라벨 숨김 — 우리 마커만 표시되도록.
                            // (확대 시 주변 상호명이 계속 바뀌어 마커 라벨처럼 보이는 문제 방지)
                            map.setPoiVisible(false)
                            map.setPoiClickable(false)

                            // 우선순위: 1) focus 좌표, 2) 현재 위치(GPS), 3) 저장된 카메라 위치, 4) 기본 위치
                            val fLat = latestFocusLat
                            val fLng = latestFocusLng
                            val uLat = userLat
                            val uLng = userLng
                            if (fLat != null && fLng != null) {
                                mapState.zoomLevel = 17
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(fLat, fLng), 17))
                                latestOnFocusConsumed()
                            } else if (uLat != null && uLng != null) {
                                // 위치가 맵 준비보다 먼저 확보된 경우: 현재 위치 중심
                                mapState.zoomLevel = 16
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(uLat, uLng), 16))
                                didCenterOnUser = true
                            } else if (viewModel?.hasSavedCameraPosition == true) {
                                val savedLat = viewModel.savedCameraLat!!
                                val savedLng = viewModel.savedCameraLng!!
                                val savedZoom = viewModel.savedCameraZoom!!
                                val center = LatLng.from(savedLat, savedLng)
                                mapState.zoomLevel = savedZoom
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(center, savedZoom))
                            } else {
                                // 기본: 등록된 프로그램 위치로, 없으면 기본 좌표(수성알파시티)
                                val valid = programsRef.filter { it.latitude != null && it.longitude != null }
                                if (valid.isNotEmpty()) {
                                    val avgLat = valid.mapNotNull { it.latitude }.average()
                                    val avgLng = valid.mapNotNull { it.longitude }.average()
                                    mapState.zoomLevel = 15
                                    map.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(avgLat, avgLng), 15))
                                    didCenterOnData = true
                                } else {
                                    val center = LatLng.from(35.842, 128.690)
                                    map.moveCamera(CameraUpdateFactory.newCenterPosition(center, 15))
                                }
                            }

                            // 맵 준비됐을 때 이미 programs가 있으면 바로 마커 추가
                            if (programsRef.isNotEmpty()) {
                                currentClusters.clear()
                                currentClusters.addAll(
                                    addClusteredMarkers(map, programsRef, mapState.zoomLevel, context)
                                )
                            }

                            // 상점 마커
                            if (storesRef.isNotEmpty()) {
                                addStoreMarkers(map, storesRef, context)
                            }

                            // 미션 마커
                            if (missionsRef.isNotEmpty()) {
                                currentMissionClusters.clear()
                                currentMissionClusters.addAll(
                                    addClusteredMissionMarkers(map, missionsRef, mapState.zoomLevel, context)
                                )
                            }

                            // 현위치 마커 (이미 위치 있으면)
                            val lat = userLat
                            val lng = userLng
                            if (lat != null && lng != null) {
                                updateLocationMarker(map, lat, lng, context)
                            }

                            // 라벨 클릭
                            map.setOnLabelClickListener { _, _, label ->
                                val labelId = label.labelId

                                // 상점 마커 클릭
                                if (labelId != null && labelId.startsWith("store_")) {
                                    val storeIdx = labelId.removePrefix("store_").toIntOrNull()
                                    val store = if (storeIdx != null) storesRef.getOrNull(storeIdx) else null
                                    if (store != null) {
                                        onStoreClick(store)
                                        return@setOnLabelClickListener true
                                    }
                                }

                                // 미션 클러스터 클릭
                                if (labelId != null && labelId.startsWith("mission_")) {
                                    val mIdx = labelId.removePrefix("mission_").toIntOrNull()
                                    val mCluster = if (mIdx != null) currentMissionClusters.getOrNull(mIdx) else null
                                    if (mCluster != null) {
                                        if (mCluster.isSingle) {
                                            // 미션이 속한 프로그램 상세로 이동 (없으면 해당 위치 줌인)
                                            val mission = mCluster.missions.first()
                                            val program = programsRef.firstOrNull { it.id == mission.programId }
                                            if (program != null) {
                                                onProgramClick(program)
                                            } else {
                                                map.moveCamera(
                                                    CameraUpdateFactory.newCenterPosition(
                                                        LatLng.from(mCluster.centerLat, mCluster.centerLng), 18
                                                    ),
                                                    CameraAnimation.from(300),
                                                )
                                            }
                                        } else {
                                            val newZoom = splitZoomForMissions(mCluster.missions, mapState.zoomLevel)
                                            map.moveCamera(
                                                CameraUpdateFactory.newCenterPosition(
                                                    LatLng.from(mCluster.centerLat, mCluster.centerLng), newZoom
                                                ),
                                                CameraAnimation.from(300),
                                            )
                                        }
                                        return@setOnLabelClickListener true
                                    }
                                }

                                // 프로그램 클러스터 클릭
                                val idx = labelId?.removePrefix("cluster_")?.toIntOrNull()
                                val cluster = if (idx != null) currentClusters.getOrNull(idx) else null

                                if (cluster != null) {
                                    if (cluster.isSingle) {
                                        onProgramClick(cluster.programs.first())
                                    } else {
                                        val pos = LatLng.from(cluster.centerLat, cluster.centerLng)
                                        val newZoom = splitZoomForPrograms(cluster.programs, mapState.zoomLevel)
                                        map.moveCamera(
                                            CameraUpdateFactory.newCenterPosition(pos, newZoom),
                                            CameraAnimation.from(300),
                                        )
                                    }
                                    true
                                } else {
                                    false
                                }
                            }

                            // 줌 체커 - programsRef 사용 (stale 클로저 방지)
                            // + 카메라 위치를 ViewModel에 저장 (네비게이션 복귀 시 복원용)
                            val handler = Handler(Looper.getMainLooper())
                            val zoomChecker = object : Runnable {
                                override fun run() {
                                    try {
                                        val newZoom = map.zoomLevel
                                        // 카메라 중심 좌표를 ViewModel에 저장 (화면 중심점 → 위경도 변환)
                                        try {
                                            val viewWidth = mapView.width
                                            val viewHeight = mapView.height
                                            if (viewWidth > 0 && viewHeight > 0) {
                                                val centerX = viewWidth / 2
                                                val centerY = viewHeight / 2
                                                val centerLatLng = map.fromScreenPoint(centerX, centerY)
                                                if (centerLatLng != null) {
                                                    viewModel?.saveCameraPosition(
                                                        centerLatLng.latitude,
                                                        centerLatLng.longitude,
                                                        newZoom,
                                                    )
                                                }
                                            }
                                        } catch (_: Exception) {}
                                        if (newZoom != mapState.zoomLevel) {
                                            mapState.zoomLevel = newZoom
                                            currentClusters.clear()
                                            currentClusters.addAll(
                                                addClusteredMarkers(map, programsRef, mapState.zoomLevel, context)
                                            )
                                            addStoreMarkers(map, storesRef, context)
                                            currentMissionClusters.clear()
                                            currentMissionClusters.addAll(
                                                addClusteredMissionMarkers(map, missionsRef, mapState.zoomLevel, context)
                                            )
                                        }
                                    } catch (_: Exception) {}
                                    handler.postDelayed(this, 300)
                                }
                            }
                            handler.postDelayed(zoomChecker, 300)
                        }
                    },
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    )

    LaunchedEffect(focusLat, focusLng) {
        if (focusLat != null && focusLng != null) {
            didCenterOnUser = true // focus 진입 시 현재위치 자동이동 생략
            mapState.kakaoMap?.let { map ->
                val pos = LatLng.from(focusLat, focusLng)
                map.moveCamera(
                    CameraUpdateFactory.newCenterPosition(pos, 17),
                    CameraAnimation.from(500),
                )
                // map이 ready된 경우에만 consume. ready 전이면 onMapReady에서 처리.
                onFocusConsumed()
            }
        }
    }
}

private fun updateLocationMarker(
    map: KakaoMap,
    lat: Double,
    lng: Double,
    context: android.content.Context,
) {
    val labelManager = map.labelManager ?: return
    val layerId = "userLocation"
    labelManager.getLayer(layerId)?.removeAll()

    val layer = labelManager.getLayer(layerId)
        ?: labelManager.addLayer(
            com.kakao.vectormap.label.LabelLayerOptions.from(layerId)
        ) ?: return

    val bitmap = createLocationMarkerBitmap(context)
    val style = LabelStyles.from(LabelStyle.from(bitmap).setApplyDpScale(false))
    val options = LabelOptions.from("user_location", LatLng.from(lat, lng))
        .setStyles(style)
        .setClickable(false)

    layer.addLabel(options)
}

private fun addClusteredMarkers(
    map: KakaoMap,
    programs: List<ProgramItem>,
    zoomLevel: Int,
    context: android.content.Context,
): List<MapCluster> {
    val labelManager = map.labelManager ?: return emptyList()

    val existingLayer = labelManager.getLayer("programMarkers")
    existingLayer?.removeAll()

    val clusters = clusterPrograms(programs, zoomLevel)
    if (clusters.isEmpty()) return clusters

    val layer = existingLayer ?: labelManager.addLayer(
        com.kakao.vectormap.label.LabelLayerOptions.from("programMarkers")
    ) ?: return clusters

    clusters.forEachIndexed { index, cluster ->
        val position = LatLng.from(cluster.centerLat, cluster.centerLng)

        val bitmap = if (cluster.isSingle) {
            val program = cluster.programs.first()
            val markerName = if (program.category == "food") {
                program.stores?.firstOrNull()?.name ?: program.name
            } else {
                program.name
            }
            createNameBubbleBitmap(markerName, context)
        } else {
            createClusterBitmap(cluster.count, context)
        }

        val style = LabelStyles.from(
            LabelStyle.from(bitmap).setApplyDpScale(false)
        )

        val options = LabelOptions.from("cluster_$index", position)
            .setStyles(style)
            .setTexts(LabelTextBuilder().setTexts("$index"))
            .setClickable(true)

        layer.addLabel(options)
    }

    return clusters
}

private fun createLocationMarkerBitmap(context: android.content.Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val outerRadius = (14 * density).toInt()
    val innerRadius = (8 * density).toInt()
    val size = outerRadius * 2

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = outerRadius.toFloat()
    val cy = outerRadius.toFloat()

    // 반투명 파란 외곽
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x402563EB // 25% opacity blue
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, outerRadius.toFloat(), outerPaint)

    // 흰색 테두리
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, (innerRadius + 2 * density).toInt().toFloat(), borderPaint)

    // 파란 내부
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, innerRadius.toFloat(), innerPaint)

    return bitmap
}

// 원본 비율: 1443×1152 (가로:세로 ≈ 5:4)
private const val DUCK_W_DP = 54f
private const val DUCK_H_DP = 43f  // 54 * 1152 / 1443 ≈ 43

// 매번 디코딩하지 않도록 캐시
private var cachedDuckBitmap: Bitmap? = null

private fun getDuckBitmap(context: android.content.Context, wPx: Int, hPx: Int): Bitmap {
    val cached = cachedDuckBitmap
    if (cached != null && cached.width == wPx && cached.height == hPx) return cached
    val src = BitmapFactory.decodeResource(context.resources, R.drawable.map_marker)
    val scaled = Bitmap.createScaledBitmap(src, wPx, hPx, true)
    cachedDuckBitmap = scaled
    return scaled
}

private fun createDuckMarkerBitmap(context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val w = (DUCK_W_DP * d).toInt()
    val h = (DUCK_H_DP * d).toInt()
    return getDuckBitmap(context, w, h)
}

private fun createClusterBitmap(count: Int, context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val duckW = (DUCK_W_DP * d).toInt()
    val duckH = (DUCK_H_DP * d).toInt()
    val badgeRadius = (14 * d)
    val badgeBorder = (3 * d)
    val extra = ((badgeRadius + badgeBorder) * 0.7f).toInt()
    val totalW = duckW + extra
    val totalH = duckH + extra

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.drawBitmap(getDuckBitmap(context, duckW, duckH), 0f, 0f, null)

    // 배지: 오른쪽 아래
    val badgeCx = duckW - badgeRadius * 0.2f
    val badgeCy = duckH - badgeRadius * 0.2f

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(badgeCx, badgeCy, badgeRadius + badgeBorder, borderPaint)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(badgeCx, badgeCy, badgeRadius, bgPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 14 * d
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    canvas.drawText(count.toString(), badgeCx, badgeCy + textPaint.textSize / 3f, textPaint)

    return bitmap
}

private fun addClusteredMissionMarkers(
    map: KakaoMap,
    missions: List<MissionItem>,
    zoomLevel: Int,
    context: android.content.Context,
): List<MissionCluster> {
    val labelManager = map.labelManager ?: return emptyList()

    val existingLayer = labelManager.getLayer("missionMarkers")
    existingLayer?.removeAll()

    val clusters = clusterMissions(missions, zoomLevel)
    if (clusters.isEmpty()) return clusters

    val layer = existingLayer ?: labelManager.addLayer(
        com.kakao.vectormap.label.LabelLayerOptions.from("missionMarkers")
    ) ?: return clusters

    clusters.forEachIndexed { index, cluster ->
        val position = LatLng.from(cluster.centerLat, cluster.centerLng)

        val bitmap = if (cluster.isSingle) {
            createMissionBubbleBitmap(cluster.missions.first().name, context)
        } else {
            createMissionClusterBitmap(cluster.count, context)
        }

        val style = LabelStyles.from(
            LabelStyle.from(bitmap).setApplyDpScale(false)
        )

        val options = LabelOptions.from("mission_$index", position)
            .setStyles(style)
            .setTexts(LabelTextBuilder().setTexts("$index"))
            .setClickable(true)

        layer.addLabel(options)
    }

    return clusters
}

private fun createMissionBubbleBitmap(name: String, context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 13 * d
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val paddingH = 12 * d
    val paddingV = 8 * d
    val boxWidth = textPaint.measureText(name) + paddingH * 2
    val boxHeight = textPaint.textSize + paddingV * 2
    val cornerRadius = 7 * d
    val pointerH = 5 * d
    val shadowPad = 5 * d

    val totalW = (boxWidth + shadowPad * 2).toInt()
    val totalH = (shadowPad + boxHeight + pointerH).toInt()

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = totalW / 2f
    val boxLeft = cx - boxWidth / 2
    val boxTop = shadowPad

    // 보라 배경 + 그림자 (미션 구분)
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4C27D0.toInt()
        style = Paint.Style.FILL
        setShadowLayer(4 * d, 0f, 2 * d, 0x33000000)
    }
    canvas.drawRoundRect(
        RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight),
        cornerRadius, cornerRadius, boxPaint
    )

    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4C27D0.toInt()
        style = Paint.Style.FILL
    }
    val pointerY = boxTop + boxHeight
    val path = Path().apply {
        moveTo(cx - 4 * d, pointerY)
        lineTo(cx, pointerY + pointerH)
        lineTo(cx + 4 * d, pointerY)
        close()
    }
    canvas.drawPath(path, pointerPaint)

    canvas.drawText(name, cx, boxTop + boxHeight / 2 + textPaint.textSize / 3f, textPaint)

    return bitmap
}

private fun createMissionClusterBitmap(count: Int, context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val radius = 20 * d
    val border = 3 * d
    val size = ((radius + border) * 2).toInt()

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val c = size / 2f

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(c, c, radius + border, borderPaint)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4C27D0.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(c, c, radius, bgPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 15 * d
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    canvas.drawText(count.toString(), c, c + textPaint.textSize / 3f, textPaint)

    return bitmap
}

private fun addStoreMarkers(
    map: KakaoMap,
    stores: List<StoreData>,
    context: android.content.Context,
) {
    val labelManager = map.labelManager ?: return

    val existingLayer = labelManager.getLayer("storeMarkers")
    existingLayer?.removeAll()

    val validStores = stores.filter { it.latitude != null && it.longitude != null }
    if (validStores.isEmpty()) return

    val layer = existingLayer ?: labelManager.addLayer(
        com.kakao.vectormap.label.LabelLayerOptions.from("storeMarkers")
    ) ?: return

    validStores.forEachIndexed { index, store ->
        val position = LatLng.from(store.latitude!!, store.longitude!!)
        val bitmap = createNameBubbleBitmap(store.name, context)

        val style = LabelStyles.from(
            LabelStyle.from(bitmap).setApplyDpScale(false)
        )

        val options = LabelOptions.from("store_$index", position)
            .setStyles(style)
            .setTexts(LabelTextBuilder().setTexts("$index"))
            .setClickable(true)

        layer.addLabel(options)
    }
}

private fun createStoreNameBubbleBitmap(name: String, context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val duckW = (DUCK_W_DP * d).toInt()
    val duckH = (DUCK_H_DP * d).toInt()

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 13 * d
        textAlign = Paint.Align.CENTER
    }
    val paddingH = 12 * d
    val paddingV = 8 * d
    val boxWidth = textPaint.measureText(name) + paddingH * 2
    val boxHeight = textPaint.textSize + paddingV * 2
    val cornerRadius = 7 * d
    val pointerH = 5 * d
    val shadowPad = 5 * d

    val totalW = maxOf(boxWidth.toInt() + (shadowPad * 2).toInt(), duckW)
    val totalH = (shadowPad + boxHeight + pointerH + duckH).toInt()

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = totalW / 2f
    val boxLeft = cx - boxWidth / 2
    val boxTop = shadowPad

    // 녹색 배경 + 그림자 (상점 구분)
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF16A34A.toInt()
        style = Paint.Style.FILL
        setShadowLayer(4 * d, 0f, 2 * d, 0x33000000)
    }
    canvas.drawRoundRect(
        RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight),
        cornerRadius, cornerRadius, boxPaint
    )

    // 포인터 삼각형
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF16A34A.toInt()
        style = Paint.Style.FILL
    }
    val pointerY = boxTop + boxHeight
    val path = Path().apply {
        moveTo(cx - 4 * d, pointerY)
        lineTo(cx, pointerY + pointerH)
        lineTo(cx + 4 * d, pointerY)
        close()
    }
    canvas.drawPath(path, pointerPaint)

    // 텍스트 (흰색)
    canvas.drawText(name, cx, boxTop + boxHeight / 2 + textPaint.textSize / 3f, textPaint)

    // 오리 이미지 (이름 박스 아래)
    val duckLeft = (totalW - duckW) / 2f
    val duckTop = boxTop + boxHeight + pointerH
    canvas.drawBitmap(getDuckBitmap(context, duckW, duckH), duckLeft, duckTop, null)

    return bitmap
}

private fun createNameBubbleBitmap(name: String, context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val duckW = (DUCK_W_DP * d).toInt()
    val duckH = (DUCK_H_DP * d).toInt()

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF121212.toInt()
        textSize = 13 * d
        textAlign = Paint.Align.CENTER
    }
    val paddingH = 12 * d
    val paddingV = 8 * d
    val boxWidth = textPaint.measureText(name) + paddingH * 2
    val boxHeight = textPaint.textSize + paddingV * 2
    val cornerRadius = 7 * d
    val pointerH = 5 * d
    val shadowPad = 5 * d  // 그림자 여백

    val totalW = maxOf(boxWidth.toInt() + (shadowPad * 2).toInt(), duckW)
    val totalH = (shadowPad + boxHeight + pointerH + duckH).toInt()

    val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = totalW / 2f
    val boxLeft = cx - boxWidth / 2
    val boxTop = shadowPad

    // 흰 배경 + 그림자
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
        setShadowLayer(4 * d, 0f, 2 * d, 0x33000000)
    }
    canvas.drawRoundRect(
        RectF(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight),
        cornerRadius, cornerRadius, boxPaint
    )

    // 포인터 삼각형 (그림자 없이)
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    val pointerY = boxTop + boxHeight
    val path = Path().apply {
        moveTo(cx - 4 * d, pointerY)
        lineTo(cx, pointerY + pointerH)
        lineTo(cx + 4 * d, pointerY)
        close()
    }
    canvas.drawPath(path, pointerPaint)

    // 텍스트
    canvas.drawText(name, cx, boxTop + boxHeight / 2 + textPaint.textSize / 3f, textPaint)

    // 오리 이미지 (이름 박스 아래)
    val duckLeft = (totalW - duckW) / 2f
    val duckTop = boxTop + boxHeight + pointerH
    canvas.drawBitmap(getDuckBitmap(context, duckW, duckH), duckLeft, duckTop, null)

    return bitmap
}
