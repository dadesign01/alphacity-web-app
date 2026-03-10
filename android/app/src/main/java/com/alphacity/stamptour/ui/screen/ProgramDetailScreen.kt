package com.alphacity.stamptour.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.ProgramDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private val GradientBlue = Brush.linearGradient(
    colors = listOf(Color(0xFF6092FF), Color(0xFF2563EB), Color(0xFF1551D3)),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

private val GradientGray = Brush.linearGradient(
    colors = listOf(Color(0xFF9CA3AF), Color(0xFF6B7280)),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

@Composable
fun ProgramDetailScreen(
    program: ProgramItem,
    onBackClick: () -> Unit = {},
    onNavigateToMap: (lat: Double, lng: Double) -> Unit = { _, _ -> },
    viewModel: ProgramDetailViewModel = hiltViewModel(),
) {
    val isFood = program.category == "food"
    val context = LocalContext.current
    val isParticipated by viewModel.isParticipated.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    var showMissionSheet by remember { mutableStateOf(false) }
    var selectedMission by remember { mutableStateOf<MissionItem?>(null) }
    var showRestrictionAlert by remember { mutableStateOf(false) }
    var restrictionMessage by remember { mutableStateOf("") }

    LaunchedEffect(program) {
        viewModel.checkParticipation(program)
        viewModel.fetchMissions(program.id)
    }

    LaunchedEffect(Unit) {
        viewModel.initTts(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        DetailHeader(
            title = when (program.category) {
                "food" -> "삼점 상세"
                "seminar" -> "세미나 상세"
                else -> "프로그램 상세"
            },
            onBackClick = onBackClick,
        )

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Thumbnail (padded, rounded)
            DetailThumbnail(
                program = program,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            )

            // Title
            Text(
                text = program.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            // Category badges
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val badgeColor = when (program.category) {
                    "food" -> Color(0xFF1BB3CE)
                    "seminar" -> Color(0xFF2563EB)
                    else -> Color(0xFF1BB3CE)
                }
                val badgeLabel = when (program.category) {
                    "food" -> "맛집"
                    "seminar" -> "세미나"
                    "exhibition" -> "전시"
                    else -> "기타"
                }
                Text(
                    text = badgeLabel,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(badgeColor)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
                if (program.status == "in_progress") {
                    val isSeminar = program.category == "seminar"
                    Text(
                        text = if (isSeminar) "참여 가능" else "운영중",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF845EDA))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }

            // Info rows (icon + text, no label)
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp),
            ) {
                if (!program.location.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = Icons.Filled.LocationOn,
                        text = program.location,
                    )
                }
                if (!program.operatingHours.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = Icons.Outlined.Schedule,
                        text = program.operatingHours,
                    )
                }
                if (!program.speaker.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = Icons.Filled.Person,
                        text = program.speaker,
                    )
                }
                if (!program.phone.isNullOrBlank()) {
                    DetailInfoRow(
                        icon = Icons.Filled.Phone,
                        text = program.phone,
                    )
                }
                DetailInfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    text = formatDetailDate(program.startDate, program.endDate),
                )
            }

            // Divider
            Divider(
                color = Color(0xFFB5B5B5),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )

            // "소개" section
            if (!program.description.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "소개",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF121212),
                    )
                    if (!isFood) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val isSpeaking by viewModel.isSpeaking.collectAsState()
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clickable {
                                    if (isSpeaking) viewModel.stopSpeaking()
                                    else program.description?.let { viewModel.speakDescription(it) }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSpeaking) {
                                TTSSoundBars()
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "TTS 재생",
                                    modifier = Modifier.size(20.dp),
                                    tint = Primary,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = program.description,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF595959),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Divider
            Divider(
                color = Color(0xFFB5B5B5),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )

            // "제휴 혜택" section
            Text(
                text = "제휴 혜택",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "스탬프 투어 참여자 10% 할인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF595959),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "쿠폰 사용 가능",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF595959),
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            // "참여 가능 미션" section (exhibition/seminar only)
            if (!isFood) {
                Divider(
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "참여 가능 미션",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF121212),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "미션 참여하기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Primary,
                        modifier = Modifier.clickable { showMissionSheet = true },
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val missions by viewModel.missions.collectAsState()
                val isMissionsLoading by viewModel.isMissionsLoading.collectAsState()

                if (isMissionsLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
                    }
                } else {
                    val available = viewModel.getAvailableMissions(program.category)
                    val disabled = viewModel.getDisabledMissions(program.category)

                    available.forEach { mission ->
                        MissionCard(mission = mission, enabled = true)
                    }

                    if (disabled.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.getDisabledMessage(program.category),
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        disabled.forEach { mission ->
                            MissionCard(mission = mission, enabled = false)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Message banner
            if (message != null) {
                Text(
                    text = message!!,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isParticipated) Color(0xFF16A34A) else Color(0xFFDC2626),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                )
            }

            // Action buttons
            if (isFood) {
                // Food: "지도에서 보기" + Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GradientBlue)
                            .clickable {
                                val lat = program.latitude ?: return@clickable
                                val lng = program.longitude ?: return@clickable
                                onNavigateToMap(lat, lng)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "지도",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "지도에서 보기",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDF7FF))
                            .clickable {
                                // Share
                                val shareText = "${program.name} - 알파시티 스탬프 투어\nalphacity://program/${program.id}"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "공유"))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "공유",
                            modifier = Modifier.size(24.dp),
                            tint = Primary,
                        )
                    }
                }
            } else {
                // Program/Seminar: "지도에서 보기" + "참여하기"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    // "지도에서 보기" button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GradientBlue)
                            .clickable {
                                val lat = program.latitude ?: return@clickable
                                val lng = program.longitude ?: return@clickable
                                onNavigateToMap(lat, lng)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Map,
                                contentDescription = "지도",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White,
                            )
                            Text(
                                text = "지도에서 보기",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                        }
                    }

                    // "참여하기" button
                    Box(
                        modifier = Modifier
                            .width(101.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isParticipated) GradientGray else GradientBlue)
                            .then(
                                if (!isParticipated && !isLoading) {
                                    Modifier.clickable { viewModel.participate(program) }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = if (isParticipated) "참여 완료" else "참여하기",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }

    // 미션 제한 알림
    if (showRestrictionAlert) {
        AlertDialog(
            onDismissRequest = { showRestrictionAlert = false },
            title = { Text("알림", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold) },
            text = { Text(restrictionMessage, fontFamily = Pretendard) },
            confirmButton = {
                TextButton(onClick = { showRestrictionAlert = false }) {
                    Text("확인", fontFamily = Pretendard, color = Primary)
                }
            },
        )
    }

    // 미션 선택 바텀시트
    if (showMissionSheet) {
        val missions by viewModel.missions.collectAsState()
        MissionSelectionBottomSheet(
            missions = missions,
            category = program.category ?: "",
            onSelectMission = { mission ->
                showMissionSheet = false
                val cat = program.category ?: ""
                if (cat == "seminar" && mission.type != "stay_time") {
                    restrictionMessage = "해당 프로그램은 체류시간 미션만 참여 가능합니다."
                    showRestrictionAlert = true
                    return@MissionSelectionBottomSheet
                }
                if (cat != "seminar" && mission.type == "stay_time") {
                    restrictionMessage = "해당 장소는 체류시간 미션 대상이 아닙니다. 다른 미션을 선택해주세요."
                    showRestrictionAlert = true
                    return@MissionSelectionBottomSheet
                }
                selectedMission = mission
            },
            onDismiss = { showMissionSheet = false },
        )
    }

    // 미션 참여 전체화면
    selectedMission?.let { mission ->
        when (mission.type) {
            "quiz" -> QuizMissionScreen(
                mission = mission,
                onDismiss = { selectedMission = null },
                onCompleted = { viewModel.fetchMissions(program.id) },
            )
            "location_auth" -> LocationMissionScreen(
                mission = mission,
                onDismiss = { selectedMission = null },
                onCompleted = { viewModel.fetchMissions(program.id) },
            )
            "stay_time" -> StayTimeMissionScreen(
                mission = mission,
                onDismiss = { selectedMission = null },
                onCompleted = { viewModel.fetchMissions(program.id) },
            )
        }
    }
}

// MARK: - Header

@Composable
private fun DetailHeader(title: String, onBackClick: () -> Unit) {
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
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
        )
    }
}

// MARK: - Thumbnail

@Composable
private fun DetailThumbnail(program: ProgramItem, modifier: Modifier = Modifier) {
    val imageUrl = program.imageUrl
    if (!imageUrl.isNullOrBlank()) {
        val fullUrl = if (imageUrl.startsWith("http")) imageUrl else BuildConfig.SERVER_URL + imageUrl
        AsyncImage(
            model = fullUrl,
            contentDescription = program.name,
            modifier = modifier
                .fillMaxWidth()
                .height(229.dp)
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(229.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = program.name.take(1),
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = Color(0xFFB5B5B5),
            )
        }
    }
}

// MARK: - Info Row

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF595959),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF595959),
            maxLines = 1,
        )
    }
}

// MARK: - Helpers

private fun formatDetailDate(startDate: String, endDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val display = SimpleDateFormat("yyyy.MM.dd(E)", Locale.KOREAN)
        val start = parser.parse(startDate.take(19))
        val end = parser.parse(endDate.take(19))
        "${start?.let { display.format(it) } ?: startDate} ~ ${end?.let { display.format(it) } ?: endDate}"
    } catch (_: Exception) {
        "${startDate.take(10)} ~ ${endDate.take(10)}"
    }
}

// MARK: - Mission Card

@Composable
private fun MissionCard(mission: MissionItem, enabled: Boolean) {
    val typeLabel = when (mission.type) {
        "quiz" -> "퀴즈"
        "location_auth" -> "위치 인증"
        "stay_time" -> "체류시간"
        else -> mission.type
    }
    val typeColor = when (mission.type) {
        "quiz" -> Color(0xFF2563EB)
        "location_auth" -> Color(0xFF16A34A)
        "stay_time" -> Color(0xFFD97706)
        else -> Color(0xFF6B7280)
    }
    val isCompleted = mission.isCompleted == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCompleted) Color(0xFFF0FFF4) else if (enabled) Color(0xFFF8F9FA) else Color(0xFFF0F0F0))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).then(if (!enabled) Modifier.alpha(0.5f) else Modifier)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF121212),
                )
                Text(
                    text = typeLabel,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (enabled) typeColor else Color(0xFFAAAAAA))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
                if (isCompleted) {
                    Text(
                        text = "완료",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF16A34A))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            val detail = when (mission.type) {
                "quiz" -> mission.question
                "location_auth" -> mission.place?.name
                "stay_time" -> "${mission.place?.name ?: ""} ${mission.stayMinutes ?: 0}분"
                else -> null
            }
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 1,
                )
            }
        }
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "완료",
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// MARK: - Mission Selection Bottom Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionSelectionBottomSheet(
    missions: List<MissionItem>,
    category: String,
    onSelectMission: (MissionItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("미션 선택", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF121212))
            Spacer(modifier = Modifier.height(4.dp))
            Text("참여할 미션을 선택하세요", fontFamily = Pretendard, fontSize = 13.sp, color = Color(0xFF828282))
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE2E2E2))
            Spacer(modifier = Modifier.height(16.dp))

            missions.forEach { mission ->
                val isAvailable = if (category == "seminar") mission.type == "stay_time" else mission.type == "quiz" || mission.type == "location_auth"
                val isCompleted = mission.isCompleted == true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCompleted) Color(0xFFF0FFF4) else Color(0xFFF8F9FA))
                        .then(if (!isCompleted) Modifier.clickable { onSelectMission(mission) } else Modifier)
                        .padding(14.dp)
                        .then(if (!isAvailable && !isCompleted) Modifier.alpha(0.5f) else Modifier),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 아이콘
                    val iconVector = when (mission.type) {
                        "quiz" -> Icons.Outlined.CalendarMonth
                        "location_auth" -> Icons.Default.LocationOn
                        "stay_time" -> Icons.Outlined.Timer
                        else -> Icons.Outlined.CalendarMonth
                    }
                    val missionColor = when (mission.type) {
                        "quiz" -> Color(0xFF2563EB)
                        "location_auth" -> Color(0xFF16A34A)
                        "stay_time" -> Color(0xFFD97706)
                        else -> Color(0xFF6B7280)
                    }
                    val bgColor = when (mission.type) {
                        "quiz" -> Color(0xFFEFF6FF)
                        "location_auth" -> Color(0xFFF0FFF4)
                        "stay_time" -> Color(0xFFFFFBEB)
                        else -> Color(0xFFF3F4F6)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Color(0xFFF0FFF4) else bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(iconVector, null, tint = if (isCompleted) Color(0xFF16A34A) else missionColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(mission.name, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF121212))
                            val typeLabel = when (mission.type) {
                                "quiz" -> "퀴즈"
                                "location_auth" -> "위치 인증"
                                "stay_time" -> "체류시간"
                                else -> mission.type
                            }
                            Text(
                                text = typeLabel,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(missionColor)
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                        val detail = when (mission.type) {
                            "quiz" -> mission.question
                            "location_auth" -> mission.place?.name
                            "stay_time" -> "${mission.place?.name ?: ""} ${mission.stayMinutes ?: 0}분"
                            else -> null
                        }
                        if (!detail.isNullOrBlank()) {
                            Text(detail, fontFamily = Pretendard, fontSize = 12.sp, color = Color(0xFF888888), maxLines = 1, modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    if (isCompleted) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    } else if (!isAvailable) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TTSSoundBars() {
    val transition = rememberInfiniteTransition(label = "tts")
    val bar1 = transition.animateFloat(
        initialValue = 5f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "bar1",
    )
    val bar2 = transition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "bar2",
    )
    val bar3 = transition.animateFloat(
        initialValue = 6f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "bar3",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(22.dp),
    ) {
        listOf(bar1, bar2, bar3).forEach { bar ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(bar.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Primary),
            )
        }
    }
}
