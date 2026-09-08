package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.web.WebTokenManager
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLImageElement
import androidx.compose.ui.viewinterop.WebElementView


private val Primary = Color(0xFF02CDF8)

enum class BottomTab(
    val title: String,
) {
    HOME("홈"),
    MAP("지도"),
    STAMP("스탬프"),
    MYPAGE("마이페이지"),
}

private sealed interface MapBackTarget {
    data object Program : MapBackTarget
    data object Festival : MapBackTarget
    data object Store : MapBackTarget
    data object Stamp : MapBackTarget
}

@Composable
fun MainScreen(
    deepLinkProgramId: Int? = null,
    onLogout: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
) {
    var selectedTab by remember {
        mutableStateOf(BottomTab.HOME)
    }

    var showProgramList by remember {
        mutableStateOf(false)
    }

    var showProgramDetail by remember {
        mutableStateOf(false)
    }

    var selectedProgramId by remember {
        mutableStateOf<Int?>(null)
    }

    var showFestivalDetail by remember {
        mutableStateOf(false)
    }

    var selectedFestivalId by remember {
        mutableStateOf<Int?>(null)
    }

    var showEventHighlight by remember {
        mutableStateOf(false)
    }

    var showMyCoupons by remember {
        mutableStateOf(false)
    }

    var showStampExchange by remember {
        mutableStateOf(false)
    }

    var showGuestDialog by remember {
        mutableStateOf(false)
    }

    var mapBackTarget by remember {
        mutableStateOf<MapBackTarget?>(null)
    }

    /*
     * 실제 브라우저 로그인 상태 기준
     *
     * access_token이 있으면 회원
     * access_token이 없으면 비회원
     */
    var isGuest by remember {
        mutableStateOf(
            !WebTokenManager.isLoggedIn()
        )
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var pendingNavigation by remember {
        mutableStateOf<(() -> Unit)?>(null)
    }

    fun navigate(action: () -> Unit) {
        if (isLoading) return

        pendingNavigation = action
        isLoading = true
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(350)

            pendingNavigation?.invoke()
            pendingNavigation = null
            isLoading = false
        }
    }

    /*
     * 로그인 상태가 변경될 수 있으므로
     * 화면 진입 시 실제 localStorage 상태를 확인합니다.
     */
    LaunchedEffect(Unit) {
        isGuest = !WebTokenManager.isLoggedIn()
    }

    deepLinkProgramId?.let { }

    /*
     * 비회원 제한 안내
     */
    if (showGuestDialog) {
        GuestRestrictionDialog(
            onConfirm = {
                showGuestDialog = false

                navigate {
                    onNavigateToRegister()
                }
            },
            onDismiss = {
                showGuestDialog = false
            },
        )
    }

    /*
     * 프로그램 상세
     */
    if (showProgramDetail) {
        val programId = selectedProgramId

        if (programId != null) {
            ProgramDetailScreen(
                programId = programId,
                onBackClick = {
                    navigate {
                        showProgramDetail = false
                        selectedProgramId = null
                        showFestivalDetail = true
                    }
                },
                onNavigateToMap = { _, _ ->
                    /*
                     * 이 기능 자체가 회원 전용이므로
                     * 여기까지 왔다면 회원 상태입니다.
                     */
                    navigate {
                        showProgramDetail = false
                        selectedProgramId = null
                        showFestivalDetail = false
                        selectedFestivalId = null

                        mapBackTarget = MapBackTarget.Program
                        selectedTab = BottomTab.MAP
                    }
                },
            )
        }

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 축제 상세
     */
    if (showFestivalDetail) {
        val festivalId = selectedFestivalId

        if (festivalId != null) {
            FestivalDetailScreen(
                festivalId = festivalId,
                onBackClick = {
                    navigate {
                        showFestivalDetail = false
                        selectedFestivalId = null
                    }
                },
                onProgramClick = { programId: Int ->
                    navigate {
                        showProgramList = false
                        selectedProgramId = programId
                        showProgramDetail = true
                    }
                },
            )
        }

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 프로그램 목록
     */
    if (showProgramList) {
        ProgramListScreen(
            onBackClick = {
                navigate {
                    showProgramList = false
                }
            },
            onProgramClick = { programId: Int ->
                navigate {
                    showProgramList = false
                    selectedProgramId = programId
                    showProgramDetail = true
                }
            },
        )

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 이벤트 하이라이트
     */
    if (showEventHighlight) {
        EventHighlightScreen(
            onBackClick = {
                navigate {
                    showEventHighlight = false
                }
            },
        )

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 내 쿠폰
     */
    if (showMyCoupons) {
        MyCouponsScreen(
            onBackClick = {
                navigate {
                    showMyCoupons = false
                }
            },
        )

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 스탬프 교환
     */
    if (showStampExchange) {
        StampExchangeScreen(
            onBackClick = {
                navigate {
                    showStampExchange = false
                }
            },
            onNavigateToCoupons = {
                navigate {
                    showStampExchange = false
                    showMyCoupons = true
                }
            },
        )

        if (isLoading) {
            LoadingOverlay()
        }

        return
    }

    /*
     * 메인
     *
     * 화면을
     *
     * 1. 콘텐츠 영역
     * 2. 하단 탭 영역
     *
     * 으로 명확하게 분리합니다.
     *
     * 콘텐츠 영역만 weight(1f)를 사용하므로
     * HomeScreen 내부의 verticalScroll 영역이
     * 하단 탭 영역까지 밀어내지 않습니다.
     */
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {

            /*
             * =================================================
             * 콘텐츠 영역
             * =================================================
             *
             * 하단 탭과 완전히 분리된 화면 영역입니다.
             */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (selectedTab) {

                    /*
                     * HOME
                     *
                     * 비회원도 접근 가능
                     */
                    BottomTab.HOME -> {
                        HomeScreen(
                            onNavigateToMyPage = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        selectedTab = BottomTab.MYPAGE
                                    }
                                }
                            },

                            onNavigateToProgramList = {
                                navigate {
                                    showProgramList = true
                                }
                            },

                            onNavigateToProgramListWithCategory = {
                                navigate {
                                    showProgramList = true
                                }
                            },

                            onNavigateToEventHighlight = {
                                navigate {
                                    showEventHighlight = true
                                }
                            },

                            onNavigateToMap = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        selectedTab = BottomTab.MAP
                                    }
                                }
                            },

                            onNavigateToStamp = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        selectedTab = BottomTab.STAMP
                                    }
                                }
                            },

                            onNavigateToCoupons = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        showMyCoupons = true
                                    }
                                }
                            },

                            onProgramClick = { programId: Int ->
                                navigate {
                                    selectedProgramId = programId
                                    showProgramDetail = true
                                }
                            },

                            onFestivalClick = { festivalId: Int ->
                                navigate {
                                    selectedFestivalId = festivalId
                                    showFestivalDetail = true
                                }
                            },

                            onBannerClick = {},

                            onGuestRestricted = {
                                showGuestDialog = true
                            },

                            isGuest = isGuest,
                        )
                    }

                    /*
                     * MAP
                     *
                     * 회원만 진입 가능
                     */
                    BottomTab.MAP -> {
                        MapScreen(
                            onProgramClick = {},
                            onStoreClick = {},

                            onNavigateToMyPage = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        selectedTab = BottomTab.MYPAGE
                                    }
                                }
                            },

                            showBack = mapBackTarget != null,

                            onBack = {
                                navigate {
                                    mapBackTarget = null
                                }
                            },
                        )
                    }

                    /*
                     * STAMP
                     *
                     * 회원만 진입 가능
                     */
                    BottomTab.STAMP -> {
                        StampScreen(
                            onNavigateToMap = { _, _ ->
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        mapBackTarget =
                                            MapBackTarget.Stamp

                                        selectedTab =
                                            BottomTab.MAP
                                    }
                                }
                            },

                            onNavigateToExchange = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        showStampExchange = true
                                    }
                                }
                            },

                            onNavigateToMyPage = {
                                if (isGuest) {
                                    showGuestDialog = true
                                } else {
                                    navigate {
                                        selectedTab =
                                            BottomTab.MYPAGE
                                    }
                                }
                            },

                            showBack = true,

                            onBack = {
                                navigate {
                                    selectedTab =
                                        BottomTab.HOME
                                }
                            },
                        )
                    }

                    /*
                     * MYPAGE
                     *
                     * 회원만 진입 가능
                     */
                    BottomTab.MYPAGE -> {
                        MyPageScreen(
                            onLogout = {
                                navigate {
                                    /*
                                     * 실제 토큰 삭제는
                                     * 상위 onLogout()에서 처리한다고
                                     * 가정합니다.
                                     */
                                    isGuest = true
                                    selectedTab =
                                        BottomTab.HOME

                                    onLogout()
                                }
                            },
                        )
                    }
                }
            }

            /*
             * =================================================
             * 하단 탭 영역
             * =================================================
             *
             * 콘텐츠 영역과 별도의 영역입니다.
             *
             * HomeScreen의 verticalScroll에 포함되지 않습니다.
             */
            HorizontalDivider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp,
            )

            BottomNavigationBar(
                selectedTab = selectedTab,
                isGuest = isGuest,

                onTabSelected = { tab ->

                    if (
                        isGuest &&
                        tab != BottomTab.HOME
                    ) {
                        showGuestDialog = true
                        return@BottomNavigationBar
                    }

                    navigate {
                        mapBackTarget = null
                        selectedTab = tab
                    }
                },
            )
        }

        /*
         * 로딩 오버레이
         */
        if (isLoading) {
            LoadingOverlay()
        }
    }
}


/* ============================================================
 * Helper Composable
 * ============================================================ */

/*
 * WebElementView를 이용한 이미지 렌더링
 *
 * composeResources/drawable에 있는 이미지를
 * 브라우저의 img element로 표시합니다.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun QuickMenuImage(
    resourceName: String,
    modifier: Modifier = Modifier,
) {
    WebElementView(
        factory = {
            (document.createElement("img") as HTMLImageElement).apply {
                src =
                    "${window.location.origin}/composeResources/composewebtest.generated.resources/drawable/$resourceName"

                alt = ""

                style.width = "100%"
                style.height = "100%"
                style.objectFit = "contain"
                style.display = "block"
            }
        },
        modifier = modifier,
        update = { image ->
            image.src =
                "${window.location.origin}/composeResources/composewebtest.generated.resources/drawable/$resourceName"

            image.alt = ""

            image.style.width = "100%"
            image.style.height = "100%"
            image.style.objectFit = "contain"
            image.style.display = "block"
        },
    )
}


@Composable
private fun BottomNavigationBar(
    selectedTab: BottomTab,
    isGuest: Boolean,
    onTabSelected: (BottomTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 24.dp,
                vertical = 8.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomTab.entries.forEach { tab ->

            val isSelected =
                selectedTab == tab

            /*
             * 각 탭에 대응하는 이미지
             */
            val imageResource = when (tab) {
                BottomTab.HOME -> "tab_home.png"
                BottomTab.MAP -> "tab_map.png"
                BottomTab.STAMP -> "tab_stamp.png"
                BottomTab.MYPAGE -> "tab_mypage.png"
            }

            Column(
                modifier = Modifier
                    .width(70.dp)
                    .clickable {
                        onTabSelected(tab)
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {

                /*
                 * 기존 문자 아이콘 대신
                 * WebElementView 기반 PNG 이미지 사용
                 */
                QuickMenuImage(
                    resourceName = imageResource,
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp),
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = tab.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (isSelected) {
                        Primary
                    } else {
                        Color(0xFF999999)
                    },
                )
            }
        }
    }
}


@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.25f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = Primary,
        )
    }
}


@Composable
private fun GuestRestrictionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "로그인이 필요합니다",
                fontWeight = FontWeight.Bold,
            )
        },

        text = {
            Text(
                text =
                    "해당 기능은 로그인 후 이용할 수 있습니다.",
            )
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                ),
            ) {
                Text("로그인")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("취소")
            }
        },
    )
}

