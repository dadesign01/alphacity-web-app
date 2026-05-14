package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.network.dto.toProgramItem
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.viewmodel.DeepLinkViewModel
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class BottomTab(val title: String, val iconRes: Int) {
    HOME("홈", R.drawable.tab_home),
    MAP("지도", R.drawable.tab_map),
    STAMP("스탬프", R.drawable.tab_stamp),
    MYPAGE("마이페이지", R.drawable.tab_mypage),
}

@Composable
fun MainScreen(
    deepLinkProgramId: Int? = null,
    onLogout: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
    tokenManager: TokenManager = hiltViewModel<MainScreenHelperViewModel>().tokenManager,
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showProgramList by remember { mutableStateOf(false) }
    var programListCategory by remember { mutableStateOf<String?>(null) }
    var showEventHighlight by remember { mutableStateOf(false) }
    var showMyCoupons by remember { mutableStateOf(false) }
    var showStampExchange by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf<ProgramItem?>(null) }
    var selectedFestival by remember { mutableStateOf<FestivalItem?>(null) }
    var selectedStore by remember { mutableStateOf<StoreData?>(null) }
    var showGuestDialog by remember { mutableStateOf(false) }
    var mapFocusLat by remember { mutableStateOf<Double?>(null) }
    var mapFocusLng by remember { mutableStateOf<Double?>(null) }

    val isGuest = !tokenManager.isLoggedIn
    val deepLinkProgram by deepLinkViewModel.program.collectAsState()
    val activity = LocalContext.current as? Activity

    // 뒤로가기: 오버레이 화면이 열려있으면 닫고, 다른 탭이면 홈으로, 홈이면 백그라운드로
    BackHandler {
        when {
            selectedStore != null -> selectedStore = null
            selectedProgram != null -> selectedProgram = null
            selectedFestival != null -> selectedFestival = null
            showStampExchange -> showStampExchange = false
            showMyCoupons -> showMyCoupons = false
            showEventHighlight -> showEventHighlight = false
            showProgramList -> { showProgramList = false; programListCategory = null }
            selectedTab != BottomTab.HOME -> selectedTab = BottomTab.HOME
            else -> activity?.moveTaskToBack(true)
        }
    }

    // 딥링크 프로그램 ID → API 로드
    LaunchedEffect(deepLinkProgramId) {
        deepLinkProgramId?.let { deepLinkViewModel.loadProgram(it) }
    }

    // API 로드 완료 → 디테일 이동
    LaunchedEffect(deepLinkProgram) {
        deepLinkProgram?.let {
            selectedProgram = it
            deepLinkViewModel.clear()
        }
    }

    selectedStore?.let { store ->
        ProgramDetailScreen(
            program = store.toProgramItem(),
            onBackClick = { selectedStore = null },
            onNavigateToMap = { lat, lng ->
                selectedStore = null
                mapFocusLat = lat
                mapFocusLng = lng
                selectedTab = BottomTab.MAP
            },
        )
        return
    }

    selectedProgram?.let { program ->
        ProgramDetailScreen(
            program = program,
            onBackClick = { selectedProgram = null },
            onNavigateToMap = { lat, lng ->
                selectedProgram = null
                mapFocusLat = lat
                mapFocusLng = lng
                selectedTab = BottomTab.MAP
            },
        )
        return
    }

    selectedFestival?.let { festival ->
        FestivalDetailScreen(
            festival = festival,
            onBackClick = { selectedFestival = null },
            onSeeAllPrograms = {
                selectedFestival = null
                showProgramList = true
            },
            onProgramClick = { program -> selectedProgram = program },
            onNavigateToMap = { lat, lng ->
                selectedFestival = null
                mapFocusLat = lat
                mapFocusLng = lng
                selectedTab = BottomTab.MAP
            },
        )
        return
    }

    if (showProgramList) {
        ProgramListScreen(
            onBackClick = { showProgramList = false; programListCategory = null },
            onProgramClick = { program -> selectedProgram = program },
            initialCategory = programListCategory,
        )
        return
    }

    if (showEventHighlight) {
        EventHighlightScreen(
            onBackClick = { showEventHighlight = false },
        )
        return
    }

    if (showMyCoupons) {
        MyCouponsScreen(
            onBackClick = { showMyCoupons = false },
        )
        return
    }

    if (showStampExchange) {
        StampExchangeScreen(
            onBackClick = { showStampExchange = false },
            onNavigateToCoupons = {
                showStampExchange = false
                showMyCoupons = true
            },
        )
        return
    }

    // 비회원 안내 다이얼로그
    if (showGuestDialog) {
        GuestRestrictionDialog(
            onConfirm = {
                showGuestDialog = false
                onNavigateToRegister()
            },
            onDismiss = { showGuestDialog = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Content (상태바 높이만큼 상단 패딩)
        Box(modifier = Modifier.weight(1f).fillMaxWidth().statusBarsPadding()) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    onNavigateToMyPage = {
                        if (isGuest) showGuestDialog = true
                        else selectedTab = BottomTab.MYPAGE
                    },
                    onNavigateToProgramList = { showProgramList = true },
                    onNavigateToProgramListWithCategory = { category ->
                        programListCategory = category
                        showProgramList = true
                    },
                    onNavigateToEventHighlight = { showEventHighlight = true },
                    onNavigateToMap = { selectedTab = BottomTab.MAP },
                    onNavigateToStamp = { selectedTab = BottomTab.STAMP },
                    onNavigateToCoupons = { showMyCoupons = true },
                    onProgramClick = { program -> selectedProgram = program },
                    onFestivalClick = { festival -> selectedFestival = festival },
                    onGuestRestricted = { showGuestDialog = true },
                    isGuest = isGuest,
                )
                BottomTab.MAP -> MapScreen(
                    onProgramClick = { program -> selectedProgram = program },
                    onStoreClick = { store -> selectedStore = store },
                    onNavigateToMyPage = {
                        if (isGuest) showGuestDialog = true
                        else selectedTab = BottomTab.MYPAGE
                    },
                    focusLat = mapFocusLat,
                    focusLng = mapFocusLng,
                    onFocusConsumed = { mapFocusLat = null; mapFocusLng = null },
                )
                BottomTab.STAMP -> StampScreen(
                    onNavigateToMap = { lat, lng ->
                        mapFocusLat = lat
                        mapFocusLng = lng
                        selectedTab = BottomTab.MAP
                    },
                    onNavigateToExchange = { showStampExchange = true },
                )
                BottomTab.MYPAGE -> MyPageScreen(onLogout = onLogout)
            }
        }

        // Bottom Tab Bar
        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (isGuest && tab != BottomTab.HOME) {
                                showGuestDialog = true
                            } else {
                                selectedTab = tab
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp),
                        alpha = if (isSelected) 1f else 0.6f,
                        colorFilter = if (isSelected) null else ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) }
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = if (isSelected) Primary else Color(0xFF999999),
                    )
                }
            }
        }
    }
}

// MARK: - 비회원 제한 안내 다이얼로그

@Composable
fun GuestRestrictionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "회원 전용 기능",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        },
        text = {
            Text(
                text = "로그인 후 이용하실 수 있습니다.\n로그인 페이지로 이동하시겠습니까?",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF666666),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "로그인",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                )
            }
        },
    )
}

// Helper ViewModel to inject TokenManager into Composable
@HiltViewModel
class MainScreenHelperViewModel @Inject constructor(
    val tokenManager: TokenManager,
) : androidx.lifecycle.ViewModel()

