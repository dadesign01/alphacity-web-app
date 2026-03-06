package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.StoreRegisterViewModel
import java.util.UUID

private enum class StoreCategory(val label: String) {
    CAFE("카페"),
    RESTAURANT("음식점"),
    SHOPPING("쇼핑"),
    HOTEL("호텔"),
    CONVENIENCE("편의시설"),
}

@Composable
fun StoreRegisterScreen(
    onBackClick: () -> Unit,
    viewModel: StoreRegisterViewModel = hiltViewModel(),
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf<StoreCategory?>(null) }

    // Step 2 form fields
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var storeAddressDetail by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var openTime by remember { mutableStateOf("09 : 00") }
    var closeTime by remember { mutableStateOf("20 : 00") }
    var storeCode by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }

    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val submitResult by viewModel.submitResult.collectAsStateWithLifecycle()

    LaunchedEffect(submitResult) {
        when (submitResult) {
            is StoreRegisterViewModel.SubmitResult.Success -> {
                currentStep = 3
                viewModel.clearResult()
            }
            is StoreRegisterViewModel.SubmitResult.Error -> {
                viewModel.clearResult()
            }
            null -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        // === Header ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_back_arrow),
                contentDescription = "뒤로",
                modifier = Modifier
                    .size(13.dp, 26.dp)
                    .clickable {
                        if (currentStep > 1) currentStep-- else onBackClick()
                    },
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "상점등록",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // === Progress Bar ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(9.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (i <= currentStep) Primary else Color(0xFFF8F8F8)
                        ),
                )
            }
        }

        // === Content ===
        when (currentStep) {
            1 -> StoreRegisterStep1(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                onNext = { if (selectedCategory != null) currentStep = 2 },
            )
            2 -> StoreRegisterStep2(
                categoryLabel = selectedCategory?.label ?: "",
                storeName = storeName,
                onStoreNameChange = { storeName = it },
                storeAddress = storeAddress,
                onStoreAddressChange = { storeAddress = it },
                storeAddressDetail = storeAddressDetail,
                onStoreAddressDetailChange = { storeAddressDetail = it },
                phoneNumber = phoneNumber,
                onPhoneNumberChange = { phoneNumber = it },
                ownerName = ownerName,
                onOwnerNameChange = { ownerName = it },
                selectedDays = selectedDays,
                onDayToggle = { day ->
                    selectedDays = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                },
                openTime = openTime,
                onOpenTimeChange = { openTime = it },
                closeTime = closeTime,
                onCloseTimeChange = { closeTime = it },
                storeCode = storeCode,
                onGenerateCode = {
                    storeCode = "#${UUID.randomUUID().toString().take(8).uppercase()}"
                },
                storeDescription = storeDescription,
                onStoreDescriptionChange = { storeDescription = it },
                onSubmit = {
                    if (!isSubmitting) {
                        viewModel.registerStore(
                            name = storeName,
                            category = selectedCategory?.name?.lowercase() ?: "",
                            ownerName = ownerName,
                            phone = phoneNumber,
                            address = storeAddress,
                            addressDetail = storeAddressDetail,
                            description = storeDescription,
                            storeCode = storeCode,
                            operatingDays = selectedDays.joinToString(","),
                            openTime = openTime.replace(" ", ""),
                            closeTime = closeTime.replace(" ", ""),
                        )
                    }
                },
            )
            3 -> StoreRegisterStep3(
                onGoHome = onBackClick,
            )
        }
    }
}

// ===================== Step 1: Category Selection =====================

@Composable
private fun StoreRegisterStep1(
    selectedCategory: StoreCategory?,
    onCategorySelect: (StoreCategory) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(140.dp))

        // Title
        Text(
            text = "상점 카테고리를 선택하세요.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "운영하시는 상점의 유형을 선택해주세요.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color(0xFF595959),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Category items
        StoreCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            StoreCategoryItem(
                category = category,
                isSelected = isSelected,
                onClick = { onCategorySelect(category) },
            )
            if (category != StoreCategory.entries.last()) {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next button
        GradientButton(
            text = "다음 단계가기",
            onClick = onNext,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun StoreCategoryItem(
    category: StoreCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val iconRes = when (category) {
        StoreCategory.CAFE -> R.drawable.icon_store_cafe
        StoreCategory.RESTAURANT -> R.drawable.icon_store_restaurant
        StoreCategory.SHOPPING -> R.drawable.icon_store_shopping
        StoreCategory.HOTEL -> R.drawable.icon_store_hotel
        StoreCategory.CONVENIENCE -> R.drawable.icon_store_convenience
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(59.dp)
            .clip(RoundedCornerShape(15.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .background(Color(0xFFEDF7FF))
                        .border(2.dp, Primary, RoundedCornerShape(15.dp))
                } else {
                    Modifier.background(Color(0xFFF8F8F8))
                }
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(22.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = category.label,
            modifier = Modifier.size(25.dp),
            contentScale = ContentScale.Fit,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                if (isSelected) Primary else Color(0xFF121212)
            ),
        )
        Spacer(modifier = Modifier.width(21.dp))
        Text(
            text = category.label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = if (isSelected) Primary else Color(0xFF121212),
        )
    }
}

// ===================== Step 2: Detail Form =====================

@Composable
private fun StoreRegisterStep2(
    categoryLabel: String,
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeAddress: String,
    onStoreAddressChange: (String) -> Unit,
    storeAddressDetail: String,
    onStoreAddressDetailChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    selectedDays: Set<String>,
    onDayToggle: (String) -> Unit,
    openTime: String,
    onOpenTimeChange: (String) -> Unit,
    closeTime: String,
    onCloseTimeChange: (String) -> Unit,
    storeCode: String,
    onGenerateCode: () -> Unit,
    storeDescription: String,
    onStoreDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(140.dp))

        // Title
        Text(
            text = "선택하신 ${categoryLabel}의 정보를 입력하세요.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "등록하실 상점의 상세 정보를 입력해주세요.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color(0xFF595959),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 대표 이미지
        Text(
            text = "대표 이미지",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(horizontal = 21.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Image upload placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(229.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFF8F8F8))
                .clickable { /* TODO: image picker */ },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.icon_upload),
                    contentDescription = "업로드",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "이미지를 업로드하세요",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFFBFBFBF),
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 상점명
        FormLabel(label = "상점명", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        FormTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            placeholder = "알파시티 카페",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 상점 주소
        FormLabel(label = "상점 주소", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        FormTextField(
            value = storeAddress,
            onValueChange = onStoreAddressChange,
            placeholder = "대구광역시 수성구 알파시티 2로 33",
        )
        Spacer(modifier = Modifier.height(6.dp))
        FormTextField(
            value = storeAddressDetail,
            onValueChange = onStoreAddressDetailChange,
            placeholder = "태왕알파시티 302호",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 전화 번호
        FormLabel(label = "전화 번호", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        FormTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = "053-123-4567",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 운영 요일
        FormLabel(label = "운영 요일", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            days.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(Color(0xFFEDF7FF))
                                    .border(1.dp, Primary, RoundedCornerShape(13.dp))
                            } else {
                                Modifier.background(Color(0xFFF8F8F8))
                            }
                        )
                        .clickable { onDayToggle(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = if (isSelected) Primary else Color(0xFF8F8F8F),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 운영 시간
        FormLabel(label = "운영 시간", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = openTime,
                    onValueChange = onOpenTimeChange,
                    textStyle = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFFBFBFBF),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    singleLine = true,
                )
            }
            Text(
                text = "~",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = closeTime,
                    onValueChange = onCloseTimeChange,
                    textStyle = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFFBFBFBF),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 대표자 이름
        FormLabel(label = "대표자 이름", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        FormTextField(
            value = ownerName,
            onValueChange = onOwnerNameChange,
            placeholder = "홍길동",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 상점 고유 코드
        FormLabel(label = "상점 고유 코드", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (storeCode.isEmpty()) {
                    Text(
                        text = "#",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFFBFBFBF),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                } else {
                    Text(
                        text = storeCode,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF121212),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(81.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDF7FF))
                    .clickable(onClick = onGenerateCode),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "생    성",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Primary,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "상점 고유 코드는 생성버튼을 클릭하면 자동 생성됩니다.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF8F8F8F),
            modifier = Modifier.padding(horizontal = 21.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 상점 설명
        FormLabel(label = "상점 설명", required = true)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp)),
        ) {
            BasicTextField(
                value = storeDescription,
                onValueChange = onStoreDescriptionChange,
                textStyle = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            )
            if (storeDescription.isEmpty()) {
                Text(
                    text = "상점에 대해 간단히 소개해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFFBFBFBF),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFFF8F8F8))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "등록 신청 후 관리자 검토를 거쳐 승인됩니다.\n승인 완료 시 알림을 보내드립니다.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit button
        GradientButton(
            text = "등록 신청하기",
            onClick = onSubmit,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ===================== Step 3: Completion =====================

@Composable
private fun StoreRegisterStep3(
    onGoHome: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Character image
        Image(
            painter = painterResource(id = R.drawable.stamp_character),
            contentDescription = null,
            modifier = Modifier.size(69.dp, 81.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "정상적으로 등록 요청되었습니다!",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "관리자의승인 후 정식 등록됩니다.\n승인 후 알림으로 알려드립니다.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF595959),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Home button
        GradientButton(
            text = "홈으로 돌아가기",
            onClick = onGoHome,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        )
    }
}

// ===================== Shared Components =====================

@Composable
private fun FormLabel(label: String, required: Boolean = false) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )
        if (required) {
            Text(
                text = " *",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Primary,
            )
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            singleLine = true,
        )
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFFBFBFBF),
                modifier = Modifier.padding(horizontal = 10.dp),
            )
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6092FF),
                        Color(0xFF2563EB),
                        Color(0xFF1551D3),
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFFF8F8F8),
        )
    }
}
