package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.FestivalDetailViewModel

@Composable
fun FestivalDetailScreen(
    festival: FestivalItem,
    onBackClick: () -> Unit = {},
    onSeeAllPrograms: () -> Unit = {},
    onProgramClick: (ProgramItem) -> Unit = {},
    onNavigateToMap: (Double, Double) -> Unit = { _, _ -> },
    viewModel: FestivalDetailViewModel = hiltViewModel(),
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(festival.id) {
        viewModel.load(festival.id)
        viewModel.selectThisFestival(festival.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_back_arrow),
                contentDescription = "뒤로",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "축제 상세",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        // Banner
        val bannerUrl = festival.bannerUrl ?: festival.imageUrl
        if (!bannerUrl.isNullOrEmpty()) {
            val fullUrl = if (bannerUrl.startsWith("http")) bannerUrl else BuildConfig.SERVER_URL + bannerUrl
            AsyncImage(
                model = fullUrl,
                contentDescription = festival.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.program_img_1),
                contentDescription = festival.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = festival.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${festival.startDate.take(10)} ~ ${festival.endDate.take(10)}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF3D608D),
            )

            festival.address?.takeIf { it.isNotEmpty() }?.let { addr ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = addr,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF8F8F8F),
                )
            }

            festival.description?.takeIf { it.isNotEmpty() }?.let { desc ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = desc,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF121212),
                    lineHeight = 22.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 지도보기 + 보러가기
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (festival.latitude != null && festival.longitude != null) {
                    Button(
                        onClick = { onNavigateToMap(festival.latitude, festival.longitude) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) { Text(text = "지도보기", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
                }
                Button(
                    onClick = onSeeAllPrograms,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text(text = "프로그램 보러가기", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
            }

            // 프로그램 미리보기
            val programs = detail?.programs.orEmpty()
            if (programs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "이 축제의 프로그램", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF121212))
                Spacer(modifier = Modifier.height(12.dp))
                programs.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                // FestivalProgramSummary → ProgramItem 최소 변환
                                onProgramClick(
                                    ProgramItem(
                                        id = p.id,
                                        name = p.name,
                                        category = p.category,
                                        imageUrl = p.imageUrl,
                                        startDate = p.startDate,
                                        endDate = p.endDate,
                                        status = p.status ?: "in_progress",
                                        location = p.location,
                                        latitude = p.latitude,
                                        longitude = p.longitude,
                                    )
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!p.imageUrl.isNullOrEmpty()) {
                            val fullUrl = if (p.imageUrl.startsWith("http")) p.imageUrl else BuildConfig.SERVER_URL + p.imageUrl
                            AsyncImage(
                                model = fullUrl,
                                contentDescription = p.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.program_img_1),
                                contentDescription = p.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(p.name, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF121212))
                            Spacer(modifier = Modifier.height(2.dp))
                            val cat = when (p.category) {
                                "food" -> "맛집"
                                "exhibition" -> "전시"
                                "seminar" -> "세미나"
                                "experience" -> "체험"
                                else -> p.category ?: ""
                            }
                            if (cat.isNotEmpty()) {
                                Text(text = cat, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF8F8F8F))
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "불러오는 중...", color = Color(0xFF8F8F8F), fontSize = 13.sp)
            }
        }
    }
}
