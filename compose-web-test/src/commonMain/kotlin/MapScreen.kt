package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.WebElementView
import com.alphacity.stamptour.viewmodel.MapViewModel
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import web.KakaoMapBridge

private val Pretendard = FontFamily.SansSerif

private data class MapCategory(
    val key: String,
    val label: String,
)

private val categories = listOf(
    MapCategory("all", "전체"),
    MapCategory("mission", "미션"),
    MapCategory("food", "맛집"),
    MapCategory("exhibition", "전시"),
    MapCategory("seminar", "세미나"),
    MapCategory("event", "이벤트"),
)

@Serializable
private data class KakaoMapItem(
    val id: Int,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val targetProgramId: Int = -1,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapScreen(
    onProgramClick: (Int) -> Unit = {},
    onStoreClick: (Int) -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    focusLat: Double? = null,
    focusLng: Double? = null,
    onFocusConsumed: () -> Unit = {},
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    val viewModel = remember {
        MapViewModel()
    }

    val programs by viewModel.filteredPrograms.collectAsState()
    val stores by viewModel.filteredStores.collectAsState()
    val missions by viewModel.filteredMissions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedProgramId by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedStoreId by remember {
        mutableStateOf<Int?>(null)
    }

    DisposableEffect(Unit) {

        onDispose {

            println(
                "[MapScreen] 지도 페이지 종료 -> Kakao Map Destroy"
            )

            KakaoMapBridge.destroyMap()

            document
                .getElementById(
                    "map-category-overlay"
                )
                ?.remove()
        }
    }

    LaunchedEffect(Unit) {

        println(
            "[MapScreen] 지도 페이지 진입 -> Kakao Map Initialize"
        )

        KakaoMapBridge.createMap(
            onItemClick = { type, id, targetProgramId ->

                when (type) {

                    "program" -> {

                        selectedProgramId =
                            id

                        onProgramClick(
                            id
                        )
                    }

                    "store" -> {

                        selectedStoreId =
                            id

                        if (
                            targetProgramId > 0
                        ) {

                            selectedProgramId =
                                targetProgramId

                            onProgramClick(
                                targetProgramId
                            )

                        } else {

                            onStoreClick(
                                id
                            )
                        }
                    }

                    "mission" -> {

                        if (
                            targetProgramId > 0
                        ) {

                            selectedProgramId =
                                targetProgramId

                            onProgramClick(
                                targetProgramId
                            )
                        }
                    }
                }
            },

            onLocation = { _, _ ->
                /*
                 * 현재 위치 marker는
                 * kakao-map.js에서 처리한다.
                 */
            },

            onLocationError = { message ->

                println(
                    "[MapScreen] 위치 권한/조회 실패: $message"
                )
            },

            onCameraChanged = {
                    latitude,
                    longitude,
                    zoom ->

                viewModel.saveCameraPosition(
                    lat = latitude,
                    lng = longitude,
                    zoom = zoom,
                )
            },
        )

        KakaoMapBridge.requestLocation()

        viewModel.fetchPrograms()
        viewModel.fetchStores()
        viewModel.fetchMissions()
    }

    LaunchedEffect(
        programs,
        stores,
        missions,
        selectedCategory,
    ) {

        val mapItems =
            buildList {

                programs.forEach { program ->

                    val latitude =
                        program.latitude

                    val longitude =
                        program.longitude

                    if (
                        latitude != null &&
                        longitude != null
                    ) {

                        add(
                            KakaoMapItem(
                                id = program.id,
                                name = program.name,
                                category =
                                    program.category ?: "",
                                latitude = latitude,
                                longitude = longitude,
                                type = "program",
                            )
                        )
                    }
                }

                stores.forEach { store ->

                    val latitude =
                        store.latitude

                    val longitude =
                        store.longitude

                    if (
                        latitude != null &&
                        longitude != null
                    ) {

                        add(
                            KakaoMapItem(
                                id = store.id,
                                name = store.name,
                                category =
                                    store.category,
                                latitude = latitude,
                                longitude = longitude,
                                type = "store",
                            )
                        )
                    }
                }

                missions.forEach { mission ->

                    val place =
                        mission.place

                    val latitude =
                        place?.latitude

                    val longitude =
                        place?.longitude

                    if (
                        latitude != null &&
                        longitude != null
                    ) {

                        add(
                            KakaoMapItem(
                                id = mission.id,
                                name = mission.name,
                                category = "mission",
                                latitude = latitude,
                                longitude = longitude,
                                type = "mission",
                                targetProgramId =
                                    mission.programId ?: -1,
                            )
                        )
                    }
                }
            }

        val json =
            Json.encodeToString(
                mapItems
            )

        println(
            "[MapScreen] Kakao Map 데이터 전달: ${mapItems.size}개"
        )

        println(
            "[MapScreen] category=$selectedCategory"
        )

        KakaoMapBridge.setItems(
            json
        )
    }

    LaunchedEffect(
        focusLat,
        focusLng
    ) {

        if (
            focusLat != null &&
            focusLng != null
        ) {

            KakaoMapBridge.moveTo(
                latitude = focusLat,
                longitude = focusLng,
                level = 17,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Transparent
            ),
    ) {

        if (showBack) {

            MapBackHeader(
                onBack = onBack,
            )

        } else {

            HomeStyleHeader(
                onProfileTap =
                    onNavigateToMyPage,
            )
        }

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Transparent
                ),
        ) {

            KakaoMapArea()

            MapCategoryOverlay(
                selectedCategory =
                    selectedCategory,

                onCategorySelected = {
                        category ->

                    selectedProgramId =
                        null

                    selectedStoreId =
                        null

                    viewModel.selectCategory(
                        category
                    )
                },
            )

            if (isLoading) {

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.Center
                        )
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        )
                        .background(
                            Color.White.copy(
                                alpha = 0.92f
                            )
                        )
                        .padding(
                            horizontal = 18.dp,
                            vertical = 12.dp,
                        ),
                ) {

                    Text(
                        text =
                            "지도 정보를 불러오는 중...",
                        fontFamily =
                            Pretendard,
                        fontWeight =
                            FontWeight.Medium,
                        fontSize =
                            13.sp,
                        color =
                            Color(0xFF333333),
                    )
                }
            }

            if (
                focusLat != null &&
                focusLng != null
            ) {

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(
                            bottom = 24.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        )
                        .background(
                            Color.Black.copy(
                                alpha = 0.75f
                            )
                        )
                        .clickable {
                            onFocusConsumed()
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp,
                        ),
                ) {

                    Text(
                        text =
                            "선택 위치로 이동",
                        fontFamily =
                            Pretendard,
                        fontWeight =
                            FontWeight.Medium,
                        fontSize =
                            12.sp,
                        color =
                            Color.White,
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * CATEGORY OVERLAY
 * ============================================================
 *
 * ★ 임시 테스트 버전
 *
 * height 지정하지 않음
 * top만 100px으로 직접 지정
 * ============================================================
 */

@Composable
private fun MapCategoryOverlay(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    DisposableEffect(selectedCategory) {

        val existing =
            document.getElementById("map-category-overlay")

        existing?.remove()

        val container =
            document.createElement("div") as HTMLElement

        container.id = "map-category-overlay"

        // ============================================================
        // OVERLAY
        // ============================================================

        container.style.position = "fixed"
        container.style.left = "0px"
        container.style.top = "100px"
        container.style.width = "100vw"

        container.style.zIndex = "9999"

        container.style.display = "flex"
        container.style.alignItems = "center"
        container.style.columnGap = "6px"

        container.style.padding = "8px 20px"
        container.style.boxSizing = "border-box"

        container.style.overflowX = "auto"
        container.style.overflowY = "hidden"

        container.style.whiteSpace = "nowrap"

        container.style.background = "transparent"

        // ============================================================
        // 실제 카테고리 데이터
        // ============================================================

        categories.forEach { category ->

            val button =
                document.createElement(
                    "button"
                ) as HTMLElement

            val isSelected =
                selectedCategory == category.key

            // ========================================================
            // 실제 데이터
            // ========================================================

            button.textContent =
                category.label

            // ========================================================
            // 기존 디자인 그대로
            // ========================================================

            button.style.flexShrink = "0"

            button.style.height = "36px"

            button.style.paddingLeft = "16px"
            button.style.paddingRight = "16px"

            button.style.borderRadius = "25px"

            button.style.border =
                "1px solid #2563EB"

            button.style.background =
                if (isSelected) {
                    "#2563EB"
                } else {
                    "#FBFCFF"
                }

            button.style.color =
                if (isSelected) {
                    "#FFFFFF"
                } else {
                    "#2563EB"
                }

            button.style.fontFamily =
                "Pretendard, sans-serif"

            button.style.fontSize =
                "14px"

            button.style.fontWeight =
                "600"

            button.style.lineHeight =
                "1"

            button.style.cursor =
                "pointer"

            button.style.margin =
                "0"

            button.style.outline =
                "none"

            // ========================================================
            // 클릭
            // ========================================================

            button.addEventListener(
                "click",
                {
                    onCategorySelected(
                        category.key
                    )
                }
            )

            container.appendChild(button)
        }

        // ============================================================
        // 순수 DOM
        // ============================================================

        document.body?.appendChild(container)

        println(
            "### MapCategoryOverlay 순수 DOM 생성"
        )

        println(
            "### selectedCategory = $selectedCategory"
        )

        println(
            "### style = ${container.getAttribute("style")}"
        )

        println(
            "### height = '${container.style.height}'"
        )

        println(
            "### width = '${container.style.width}'"
        )

        onDispose {

            container.remove()

            println(
                "### MapCategoryOverlay 순수 DOM 제거"
            )
        }
    }
}


/*
 * ============================================================
 * Home Style Header
 * ============================================================
 */

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeStyleHeader(
    onProfileTap: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White
            )
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        WebHeaderImage(
            resourceName =
                "header_logo.png",
            width =
                42.dp,
            height =
                42.dp,
        )

        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )

        Text(
            text =
                "올리모아",
            color =
                Color(0xFF121212),
            fontSize =
                20.sp,
            fontWeight =
                FontWeight.Bold,
        )

        Spacer(
            modifier =
                Modifier.weight(
                    1f
                )
        )

        Box(
            modifier = Modifier
                .size(
                    42.dp
                )
                .clip(
                    CircleShape
                )
                .clickable {
                    onProfileTap()
                },
            contentAlignment =
                Alignment.Center,
        ) {

            WebHeaderImage(
                resourceName =
                    "icon_profile.png",
                width =
                    42.dp,
                height =
                    42.dp,
            )
        }
    }
}


/*
 * ============================================================
 * Map Back Header
 * ============================================================
 */

@Composable
private fun MapBackHeader(
    onBack: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White
            )
            .padding(
                start = 15.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        Text(
            text =
                "‹",
            fontFamily =
                Pretendard,
            fontSize =
                34.sp,
            color =
                Color(0xFF121212),
            modifier = Modifier
                .size(
                    width = 13.dp,
                    height = 26.dp,
                )
                .clickable {
                    onBack()
                },
        )

        Spacer(
            modifier =
                Modifier.width(
                    14.dp
                )
        )

        Text(
            text =
                "지도 상세보기",
            fontFamily =
                Pretendard,
            fontWeight =
                FontWeight.SemiBold,
            fontSize =
                18.sp,
            color =
                Color(0xFF121212),
            letterSpacing =
                (-0.36).sp,
        )

        Spacer(
            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}


/*
 * ============================================================
 * Web Header Image
 * ============================================================
 */

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun WebHeaderImage(
    resourceName: String,
    width: Dp,
    height: Dp,
) {
    WebElementView(
        factory = {

            (
                    document.createElement(
                        "img"
                    ) as HTMLImageElement
                    ).apply {

                    src =
                        "${window.location.origin}/composeResources/composewebtest.generated.resources/drawable/$resourceName"

                    alt =
                        ""

                    style.width =
                        "${width.value}px"

                    style.height =
                        "${height.value}px"

                    style.objectFit =
                        "contain"

                    style.display =
                        "block"
                }
        },

        modifier =
            Modifier.size(
                width =
                    width,
                height =
                    height,
            ),

        update = {
                image ->

            image.src =
                "${window.location.origin}/composeResources/composewebtest.generated.resources/drawable/$resourceName"

            image.alt =
                ""

            image.style.width =
                "${width.value}px"

            image.style.height =
                "${height.value}px"

            image.style.objectFit =
                "contain"

            image.style.display =
                "block"
        },
    )
}


/*
 * ============================================================
 * Kakao Map
 * ============================================================
 */

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun KakaoMapArea() {

    WebElementView<HTMLElement>(
        factory = {

            (
                    document.createElement(
                        "div"
                    ) as HTMLElement
                    ).apply {

                    id =
                        "kakao-map-root"

                    style.width =
                        "100%"

                    style.height =
                        "100%"

                    style.position =
                        "relative"

                    style.overflowX =
                        "hidden"

                    style.overflowY =
                        "hidden"

                    style.background =
                        "transparent"

                    style.zIndex =
                        "0"

                    style.display =
                        "none"

                    style.visibility =
                        "hidden"

                    style.opacity =
                        "0"
                }
        },

        modifier =
            Modifier.fillMaxSize(),

        update = {
                mapRoot ->

            mapRoot.style.width =
                "100%"

            mapRoot.style.height =
                "100%"

            mapRoot.style.position =
                "relative"

            mapRoot.style.background =
                "transparent"

            mapRoot.style.zIndex =
                "0"
        },
    )
}