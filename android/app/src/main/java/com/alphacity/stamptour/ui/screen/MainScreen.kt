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
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.viewmodel.DeepLinkViewModel
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
    var selectedProgram by remember { mutableStateOf<ProgramItem?>(null) }
    var showGuestDialog by remember { mutableStateOf(false) }
    var mapFocusLat by remember { mutableStateOf<Double?>(null) }
    var mapFocusLng by remember { mutableStateOf<Double?>(null) }

    val isGuest = !tokenManager.isLoggedIn
    val deepLinkProgram by deepLinkViewModel.program.collectAsState()

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
        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                    onGuestRestricted = { showGuestDialog = true },
                    isGuest = isGuest,
                )
                BottomTab.MAP -> MapScreen(
                    onProgramClick = { program -> selectedProgram = program },
                    onNavigateToMyPage = {
                        if (isGuest) showGuestDialog = true
                        else selectedTab = BottomTab.MYPAGE
                    },
                    focusLat = mapFocusLat,
                    focusLng = mapFocusLng,
                    onFocusConsumed = { mapFocusLat = null; mapFocusLng = null },
                )
                BottomTab.STAMP -> StampScreen()
                BottomTab.MYPAGE -> MyPageScreen(onLogout = onLogout)
            }
        }

        // Bottom Tab Bar
        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 8.dp, bottom = 20.dp),
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
                text = "회원가입 후 이용하실 수 있습니다.\n회원가입 페이지로 이동하시겠습니까?",
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
                    text = "회원가입",
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

