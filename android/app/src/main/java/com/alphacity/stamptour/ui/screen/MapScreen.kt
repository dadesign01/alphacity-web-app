package com.alphacity.stamptour.ui.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
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
import com.alphacity.stamptour.viewmodel.MapViewModel
import android.os.Handler
import android.os.Looper
import kotlin.math.floor
import kotlin.math.min

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

    // 줌 18 이상이면 클러스터링 안함
    if (zoomLevel >= 18) {
        return valid.map { MapCluster(it.latitude!!, it.longitude!!, listOf(it)) }
    }

    val gridSize = when (zoomLevel) {
        in 0..10 -> 0.05
        in 11..12 -> 0.02
        13 -> 0.01
        14 -> 0.005
        15 -> 0.003
        16 -> 0.0015
        else -> 0.0008
    }

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

private data class MapCategory(val key: String, val label: String)

private val categories = listOf(
    MapCategory("all", "전체"),
    MapCategory("food", "맛집"),
    MapCategory("exhibition", "전시"),
    MapCategory("seminar", "세미나"),
    MapCategory("event", "이벤트"),
)

@Composable
fun MapScreen(
    onProgramClick: (ProgramItem) -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
    focusLat: Double? = null,
    focusLng: Double? = null,
    onFocusConsumed: () -> Unit = {},
) {
    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPrograms()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Header
        MapHeader(onProfileClick = onNavigateToMyPage)

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // Map + Category overlay
        Box(modifier = Modifier.fillMaxSize()) {
            KakaoMapContent(
                programs = filteredPrograms,
                onProgramClick = onProgramClick,
                focusLat = focusLat,
                focusLng = focusLng,
                onFocusConsumed = onFocusConsumed,
            )

            // Category filter tabs
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
private fun MapHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            text = "알파스탬프",
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

@Composable
private fun KakaoMapContent(
    programs: List<ProgramItem>,
    onProgramClick: (ProgramItem) -> Unit,
    focusLat: Double? = null,
    focusLng: Double? = null,
    onFocusConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var kakaoMapRef: KakaoMap? = remember { null }
    var currentZoomLevel = remember { 15 }
    var currentClusters = remember { mutableListOf<MapCluster>() }

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
                            kakaoMapRef = map
                            val center = LatLng.from(35.842, 128.690)
                            map.moveCamera(CameraUpdateFactory.newCenterPosition(center, 15))

                            currentClusters.clear()
                            currentClusters.addAll(
                                addClusteredMarkers(map, programs, currentZoomLevel, context)
                            )

                            // 라벨 클릭 이벤트
                            map.setOnLabelClickListener { _, _, label ->
                                val labelId = label.labelId
                                val idx = labelId?.removePrefix("cluster_")?.toIntOrNull()
                                val cluster = if (idx != null) currentClusters.getOrNull(idx) else null

                                if (cluster != null) {
                                    if (cluster.isSingle) {
                                        onProgramClick(cluster.programs.first())
                                    } else {
                                        // 클러스터 탭 → 줌 인
                                        val pos = LatLng.from(cluster.centerLat, cluster.centerLng)
                                        val newZoom = min(currentZoomLevel + 2, 17)
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

                            // 줌 변경 감지 폴링 (300ms 간격)
                            val handler = Handler(Looper.getMainLooper())
                            val zoomChecker = object : Runnable {
                                override fun run() {
                                    try {
                                        val newZoom = map.zoomLevel
                                        if (newZoom != currentZoomLevel) {
                                            currentZoomLevel = newZoom
                                            currentClusters.clear()
                                            currentClusters.addAll(
                                                addClusteredMarkers(map, programs, currentZoomLevel, context)
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

    LaunchedEffect(programs) {
        kakaoMapRef?.let { map ->
            currentClusters.clear()
            currentClusters.addAll(
                addClusteredMarkers(map, programs, currentZoomLevel, context)
            )
        }
    }

    LaunchedEffect(focusLat, focusLng) {
        if (focusLat != null && focusLng != null) {
            kakaoMapRef?.let { map ->
                val pos = LatLng.from(focusLat, focusLng)
                map.moveCamera(
                    CameraUpdateFactory.newCenterPosition(pos, 17),
                    CameraAnimation.from(500),
                )
            }
            onFocusConsumed()
        }
    }
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
            createNameBubbleBitmap(cluster.programs.first().name, context)
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

private fun createClusterBitmap(count: Int, context: android.content.Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val outerRadius = (22 * density).toInt()
    val innerRadius = (16 * density).toInt()
    val size = outerRadius * 2

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = outerRadius.toFloat()
    val cy = outerRadius.toFloat()

    // 외곽 반투명 원
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x4D2563EB // 30% opacity
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, outerRadius.toFloat(), outerPaint)

    // 내부 원
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2563EB.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, innerRadius.toFloat(), innerPaint)

    // 숫자
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 14 * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textY = cy + (textPaint.textSize / 3)
    canvas.drawText(count.toString(), cx, textY, textPaint)

    return bitmap
}

private fun createNameBubbleBitmap(name: String, context: android.content.Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val primaryColor = 0xFF2563EB.toInt()
    val pointerHeight = (6 * density).toInt()
    val paddingH = 10 * density
    val paddingV = 5 * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 10 * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textWidth = textPaint.measureText(name)
    val boxWidth = (textWidth + paddingH * 2).toInt()
    val boxHeight = (textPaint.textSize + paddingV * 2).toInt()
    val cornerRadius = boxHeight / 2f
    val totalHeight = boxHeight + pointerHeight

    val bitmap = Bitmap.createBitmap(boxWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = boxWidth / 2f

    // 배경 라운드 박스
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        style = Paint.Style.FILL
    }
    val boxRect = android.graphics.RectF(0f, 0f, boxWidth.toFloat(), boxHeight.toFloat())
    canvas.drawRoundRect(boxRect, cornerRadius, cornerRadius, bgPaint)

    // 아래 삼각형 포인터
    val path = Path().apply {
        moveTo(cx - 4 * density, boxHeight - 1f)
        lineTo(cx, totalHeight.toFloat())
        lineTo(cx + 4 * density, boxHeight - 1f)
        close()
    }
    canvas.drawPath(path, bgPaint)

    // 텍스트
    val textY = boxHeight / 2f + textPaint.textSize / 3f
    canvas.drawText(name, cx, textY, textPaint)

    return bitmap
}
