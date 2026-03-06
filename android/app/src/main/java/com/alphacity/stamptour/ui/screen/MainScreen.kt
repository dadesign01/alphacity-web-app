package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
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
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.viewmodel.DeepLinkViewModel

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
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showProgramList by remember { mutableStateOf(false) }
    var showEventHighlight by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf<ProgramItem?>(null) }

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
        )
        return
    }

    if (showProgramList) {
        ProgramListScreen(
            onBackClick = { showProgramList = false },
            onProgramClick = { program -> selectedProgram = program },
        )
        return
    }

    if (showEventHighlight) {
        EventHighlightScreen(
            onBackClick = { showEventHighlight = false },
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    onNavigateToMyPage = { selectedTab = BottomTab.MYPAGE },
                    onNavigateToProgramList = { showProgramList = true },
                    onNavigateToEventHighlight = { showEventHighlight = true },
                )
                BottomTab.MAP -> MapScreen(
                    onProgramClick = { program -> selectedProgram = program },
                    onNavigateToMyPage = { selectedTab = BottomTab.MYPAGE },
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
                        .clickable { selectedTab = tab },
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

