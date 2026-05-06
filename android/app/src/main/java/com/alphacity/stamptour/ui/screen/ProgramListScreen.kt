package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.viewmodel.ProgramListViewModel
import androidx.compose.foundation.border
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgramListScreen(
    onBackClick: () -> Unit = {},
    onProgramClick: (ProgramItem) -> Unit = {},
    initialCategory: String? = null,
    viewModel: ProgramListViewModel = hiltViewModel(),
) {
    val programs by viewModel.programs.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val festivals by viewModel.festivals.collectAsState()
    val selectedFestivalId by viewModel.selectedFestivalId.collectAsState()

    LaunchedEffect(Unit) {
        if (initialCategory != null) {
            viewModel.selectCategory(initialCategory)
        } else {
            viewModel.fetchPrograms()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Header
        ProgramListHeader(onBackClick = onBackClick)

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // Category Tabs + Content (scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 축제 선택 드롭다운
            FestivalSelectBar(
                festivals = festivals,
                selectedId = selectedFestivalId,
                onSelect = { id -> viewModel.selectFestival(id) },
            )

            // Category Tabs
            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
            )

            // Program List
            if (programs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 300.dp)
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = "등록된 프로그램이 없습니다",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            } else {
                programs.forEachIndexed { index, program ->
                    Box(modifier = Modifier.clickable { onProgramClick(program) }) {
                        ProgramListCard(
                            program = program,
                            isSeminar = selectedCategory == "seminar",
                        )
                    }
                    if (index < programs.lastIndex) {
                        Divider(
                            color = Color(0xFFB5B5B5),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer - always at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "© 2026 Alpha Stamp. All rights reserved.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

// MARK: - Header

@Composable
private fun ProgramListHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_back_arrow),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .size(width = 13.dp, height = 26.dp)
                .clickable { onBackClick() },
            colorFilter = ColorFilter.tint(Color(0xFF121212)),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "오늘의 프로그램",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
        )
    }
}

// MARK: - Category Tabs

@Composable
private fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    data class CategoryItem(val key: String, val label: String, val iconRes: Int)

    val categories = listOf(
        CategoryItem("food", "맛집", R.drawable.icon_food),
        CategoryItem("exhibition", "전시", R.drawable.icon_exhibition),
        CategoryItem("seminar", "세미나", R.drawable.icon_seminar),
        CategoryItem("experience", "체험", R.drawable.icon_activity),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category.key
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) Color(0xFF121212) else Color(0xFFF8F8F8))
                    .clickable { onCategorySelected(category.key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = category.label,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(
                        if (isSelected) Color.White else Color(0xFF121212)
                    ),
                )
                Text(
                    text = category.label,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color(0xFF121212),
                )
            }
        }
    }
}

// MARK: - Program Card

@Composable
private fun ProgramListCard(
    program: ProgramItem,
    isSeminar: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // Thumbnail image with coupon badge
        Box(modifier = Modifier.fillMaxWidth()) {
            val imageUrl = program.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                val fullUrl = if (imageUrl.startsWith("http")) imageUrl else BuildConfig.SERVER_URL + imageUrl
                AsyncImage(
                    model = fullUrl,
                    contentDescription = program.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                // Placeholder image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = program.name.take(1),
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFFB5B5B5),
                    )
                }
            }

            // Coupon badge overlay
            if (program.hasCoupon == true) {
                Text(
                    text = "쿠폰",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFFF6B35))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subcategory tag + Status badge + Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Subcategory tag
            if (!program.subcategory.isNullOrBlank()) {
                Text(
                    text = program.subcategory,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            // Status badge
            val (badgeText, badgeBgColor, badgeTextColor) = getStatusBadge(program.status, isSeminar)
            Text(
                text = badgeText,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = badgeTextColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )

            // Title
            Text(
                text = program.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        // Description
        if (!program.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = program.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Time / Date info
        Spacer(modifier = Modifier.height(5.dp))
        val timeInfo = buildTimeInfo(program, isSeminar)
        Text(
            text = timeInfo,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF828282),
        )

        // Location bar
        if (!program.location.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_location_pin),
                    contentDescription = "위치",
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF8F8F8F)),
                )
                Text(
                    text = program.location,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// MARK: - Helpers

private data class BadgeStyle(val text: String, val bgColor: Color, val textColor: Color)

private fun getStatusBadge(status: String, isSeminar: Boolean): BadgeStyle {
    return when {
        status == "in_progress" && isSeminar -> BadgeStyle("참여 가능", Color(0xFFEDF7FF), Color(0xFF2563EB))
        status == "in_progress" -> BadgeStyle("운영중", Color(0xFFEDF7FF), Color(0xFF2563EB))
        status == "scheduled" -> BadgeStyle("운영예정", Color(0xFFF8F8F8), Color(0xFF8F8F8F))
        else -> BadgeStyle("종료", Color(0xFFF8F8F8), Color(0xFF8F8F8F))
    }
}

private fun buildTimeInfo(program: ProgramItem, isSeminar: Boolean): String {
    val hours = program.operatingHours ?: ""
    return if (isSeminar && !program.speaker.isNullOrBlank()) {
        if (hours.isNotBlank()) "$hours    |   ${program.speaker}" else program.speaker
    } else {
        val dateRange = formatDateRange(program.startDate, program.endDate)
        if (hours.isNotBlank()) "$hours    |   $dateRange" else dateRange
    }
}

private fun formatDateRange(startDate: String, endDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val display = SimpleDateFormat("M/dd(E)", Locale.KOREAN)
        val start = parser.parse(startDate.take(19))
        val end = parser.parse(endDate.take(19))
        "${start?.let { display.format(it) } ?: startDate} ~ ${end?.let { display.format(it) } ?: endDate}"
    } catch (_: Exception) {
        "${startDate.take(10)} ~ ${endDate.take(10)}"
    }
}

@Composable
private fun FestivalSelectBar(
    festivals: List<FestivalItem>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = festivals.find { it.id == selectedId }?.name ?: "전체 축제"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFD0D5DD), shape = RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "축제 선택",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF8F8F8F),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = selectedName,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (expanded) "▲" else "▼",
                color = Color(0xFF8F8F8F),
                fontSize = 12.sp,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(text = { Text("전체 축제") }, onClick = { onSelect(null); expanded = false })
            festivals.forEach { f ->
                DropdownMenuItem(text = { Text(f.name) }, onClick = { onSelect(f.id); expanded = false })
            }
        }
    }
}
