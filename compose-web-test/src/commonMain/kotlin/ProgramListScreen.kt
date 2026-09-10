@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.HomeRepository
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import composewebtest.generated.resources.selectbox_arrow

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private const val API_BASE_URL =
    "https://ollymoa-server.vercel.app"

// ============================================================
// Image URL
// ============================================================

private fun imageUrl(
    path: String?,
): String? {
    if (path.isNullOrBlank()) {
        return null
    }

    return if (
        path.startsWith("http://") ||
        path.startsWith("https://")
    ) {
        path
    } else {
        "$API_BASE_URL${if (path.startsWith("/")) path else "/$path"}"
    }
}

// ============================================================
// Program List Screen
// ============================================================

@Composable
fun ProgramListScreen(
    festivalId: Int? = null,
    onBackClick: () -> Unit = {},
    onProgramClick: (Int) -> Unit = {},
    initialCategory: String? = null,
) {
    val repository = remember {
        HomeRepository()
    }

    var festivals by remember {
        mutableStateOf<List<FestivalItem>>(emptyList())
    }

    var selectedFestivalId by remember {
        mutableStateOf(festivalId)
    }

    var selectedCategory by remember {
        mutableStateOf(
            initialCategory ?: "exhibition"
        )
    }

    var programs by remember {
        mutableStateOf<List<ProgramItem>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // ========================================================
    // Festival
    // ========================================================

    LaunchedEffect(Unit) {
        repository
            .getFestivals()
            .onSuccess {
                festivals = it

                if (
                    selectedFestivalId == null &&
                    it.isNotEmpty()
                ) {
                    selectedFestivalId = it.first().id
                }
            }
            .onFailure {
                println(
                    "[ProgramListScreen] 축제 조회 실패: ${it.message}"
                )
            }
    }

    // ========================================================
    // Programs
    // ========================================================

    LaunchedEffect(
        selectedFestivalId,
        selectedCategory,
    ) {
        isLoading = true
        errorMessage = null

        repository
            .getPrograms(
                festivalId = selectedFestivalId,
                category = selectedCategory,
            )
            .onSuccess {
                programs = it
            }
            .onFailure {
                programs = emptyList()

                errorMessage =
                    it.message
                        ?: "프로그램을 불러오지 못했습니다."

                println(
                    "[ProgramListScreen] 프로그램 조회 실패: ${it.message}"
                )
            }

        isLoading = false
    }

    // ========================================================
    // Screen
    // ========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        ProgramListHeader(
            onBackClick = onBackClick,
        )

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        ) {

            // ==================================================
            // Festival Select
            // ==================================================

            FestivalSelectBar(
                festivals = festivals,
                selectedId = selectedFestivalId,
                onSelect = {
                    selectedFestivalId = it
                },
            )

            // ==================================================
            // Category
            // ==================================================

            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                },
            )

            // ==================================================
            // Program List
            // ==================================================

            if (isLoading) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                    )
                }

            } else if (
                programs.isEmpty()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(
                            minHeight = 300.dp
                        )
                        .padding(
                            top = 80.dp
                        ),
                    contentAlignment =
                        Alignment.TopCenter,
                ) {
                    Text(
                        text =
                            errorMessage
                                ?: "등록된 프로그램이 없습니다",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF7D7D7D),
                    )
                }

            } else {

                programs.forEach { program ->

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProgramClick(
                                    program.id
                                )
                            },
                    ) {
                        ProgramListCard(
                            program = program,
                        )
                    }

                    HorizontalDivider(
                        color = Color(0xFFB5B5B5),
                        thickness = 0.5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp
                            ),
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            // ==================================================
            // Footer
            // ==================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF9F9F9)
                    )
                    .padding(
                        vertical = 20.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {
                Text(
                    text =
                        "2026 OLLYMOA. All rights reserved.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF7D7D7D),
                )
            }
        }
    }
}

// ============================================================
// Header
// ============================================================

@Composable
private fun ProgramListHeader(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier
                .size(
                    width = 8.dp,
                    height = 15.dp,
                )
                .clickable {
                    onBackClick()
                },
            contentAlignment =
                Alignment.Center,
        ) {
            Image(
                painter = painterResource(
                    Res.drawable.header_left_arrow
                ),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(
                    width = 8.dp,
                    height = 15.dp,
                ),
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = "오늘의 프로그램",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF121212),
        )
    }
}

// ============================================================
// Category Tabs
// ============================================================

@Composable
private fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    val categories = listOf(
        "exhibition" to "전시",
        "seminar" to "세미나",
        "food" to "맛집",
        "experience" to "체험",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
        horizontalArrangement =
            Arrangement.spacedBy(7.dp),
    ) {
        categories.forEach { (key, label) ->

            val isSelected =
                selectedCategory == key

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(25.dp)
                    )
                    .background(
                        if (isSelected) {
                            Color(0xFF121212)
                        } else {
                            Color(0xFFF8F8F8)
                        }
                    )
                    .clickable {
                        onCategorySelected(key)
                    }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 8.dp,
                    ),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color =
                        if (isSelected) {
                            Color(0xFFF8F8F8)
                        } else {
                            Color(0xFF212121)
                        },
                )
            }
        }
    }
}

// ============================================================
// Program Card
// ============================================================

@Composable
private fun ProgramListCard(
    program: ProgramItem,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 15.dp,
            ),
    ) {

        // ====================================================
        // Image
        // ====================================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    Color(0xFFE8E8E8)
                ),
            contentAlignment =
                Alignment.Center,
        ) {
            val programImage =
                imageUrl(
                    program.imageUrl
                )

            if (
                !programImage.isNullOrBlank()
            ) {
                AsyncImage(
                    model = programImage,
                    contentDescription = program.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(15.dp)
                        ),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = program.name.take(1),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFFB5B5B5),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // ====================================================
        // Status
        // ====================================================

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFF2563EB),
                    shape = RoundedCornerShape(15.dp),
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 5.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = getStatusText(
                    program.status
                ),
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF2563EB),
            )
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        // ====================================================
        // Program Name
        // ====================================================

        Text(
            text = program.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF212121),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // ====================================================
        // Description
        // ====================================================

        if (
            !program.description.isNullOrBlank()
        ) {
            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = program.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF7D7D7D),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ====================================================
        // Date / Operating Hours
        // ====================================================

        Text(
            text = buildDateTimeText(
                startDate = program.startDate,
                endDate = program.endDate,
                operatingHours =
                    program.operatingHours,
            ),
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF7D7D7D),
        )
    }
}

// ============================================================
// Status Text
// ============================================================

private fun getStatusText(
    status: String?,
): String {
    return when (status) {
        "in_progress" -> "진행중"
        "scheduled" -> "진행 예정"
        "completed" -> "종료"
        "ended" -> "종료"
        else -> "진행중"
    }
}

// ============================================================
// Date / Time
// ============================================================

private fun buildDateTimeText(
    startDate: String?,
    endDate: String?,
    operatingHours: String?,
): String {
    val date =
        formatProgramDate(
            startDate
        )

    val time =
        operatingHours
            ?.takeIf {
                it.isNotBlank()
            }
            ?: ""

    return when {
        date.isNotBlank() &&
                time.isNotBlank() ->
            "$date  |  $time"

        date.isNotBlank() ->
            date

        time.isNotBlank() ->
            time

        else ->
            ""
    }
}

// ============================================================
// Program Date
// ============================================================

private fun formatProgramDate(
    startDate: String?,
): String {
    if (
        startDate.isNullOrBlank()
    ) {
        return ""
    }

    val date =
        startDate.take(10)

    if (
        date.length < 10
    ) {
        return date
    }

    val year =
        date.substring(
            0,
            4
        )

    val month =
        date.substring(
            5,
            7
        )

    val day =
        date.substring(
            8,
            10
        )

    val weekday =
        getWeekday(
            year.toIntOrNull()
                ?: return "$year.$month.$day",
            month.toIntOrNull()
                ?: return "$year.$month.$day",
            day.toIntOrNull()
                ?: return "$year.$month.$day",
        )

    return "$year.$month.$day($weekday)"
}

// ============================================================
// Weekday
// ============================================================

private fun getWeekday(
    year: Int,
    month: Int,
    day: Int,
): String {
    var y = year
    var m = month

    if (m < 3) {
        y -= 1
        m += 12
    }

    val k = y % 100
    val j = y / 100

    val h =
        (
                day +
                        (13 * (m + 1)) / 5 +
                        k +
                        k / 4 +
                        j / 4 +
                        5 * j
                ) % 7

    return when (h) {
        0 -> "토"
        1 -> "일"
        2 -> "월"
        3 -> "화"
        4 -> "수"
        5 -> "목"
        else -> "금"
    }
}

// ============================================================
// Festival Select
// ============================================================

@Composable
private fun FestivalSelectBar(
    festivals: List<FestivalItem>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedName =
        festivals
            .find {
                it.id == selectedId
            }
            ?.name

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 10.dp,
            ),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(15.dp),
                )
                .border(
                    1.dp,
                    Color(0xFFDBDBDB),
                    RoundedCornerShape(15.dp),
                )
                .clickable {
                    expanded = !expanded
                }
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Text(
                text = "축제 선택",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color(0xFFB5B5B5),
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = selectedName ?: "",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            SelectBoxArrow(
                expanded = expanded,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            festivals.forEach { festival ->

                DropdownMenuItem(
                    text = {
                        Text(
                            text = festival.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color(0xFF121212),
                        )
                    },
                    onClick = {
                        onSelect(
                            festival.id
                        )
                        expanded = false
                    },
                )
            }
        }
    }
}

// ============================================================
// Select Box Arrow
// ============================================================

@Composable
private fun SelectBoxArrow(
    expanded: Boolean,
) {
    Image(
        painter = painterResource(
            Res.drawable.selectbox_arrow
        ),
        contentDescription = "선택",
        modifier = Modifier
            .size(
                width = 6.dp,
                height = 10.dp,
            )
            .graphicsLayer {
                scaleY =
                    if (expanded) {
                        -1f
                    } else {
                        1f
                    }
            },
    )
}