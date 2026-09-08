import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private enum class StoreCategory(
    val label: String,
    val icon: String,
) {
    CAFE("카페", "☕"),
    RESTAURANT("음식점", "🍽"),
    SHOPPING("쇼핑", "🛍"),
    HOTEL("호텔", "🏨"),
    CONVENIENCE("편의시설", "🏪"),
}

@Composable
fun StoreRegisterScreen(
    onBackClick: () -> Unit = {},
) {
    var currentStep by remember {
        mutableStateOf(1)
    }

    var selectedCategory by remember {
        mutableStateOf<StoreCategory?>(null)
    }

    var storeName by remember {
        mutableStateOf("")
    }

    var storeAddress by remember {
        mutableStateOf("")
    }

    var storeAddressDetail by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("")
    }

    var ownerName by remember {
        mutableStateOf("")
    }

    var selectedDays by remember {
        mutableStateOf(setOf<String>())
    }

    var openTime by remember {
        mutableStateOf("09:00")
    }

    var closeTime by remember {
        mutableStateOf("20:00")
    }

    var storeCode by remember {
        mutableStateOf("")
    }

    var storeDescription by remember {
        mutableStateOf("")
    }

    var imageSelected by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    if (currentStep > 1) {
                        currentStep--
                    } else {
                        onBackClick()
                    }
                },
            )

            Spacer(
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = "상점등록",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        // Progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 6.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(9.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (i <= currentStep) {
                                Primary
                            } else {
                                Color(0xFFF8F8F8)
                            }
                        ),
                )
            }
        }

        when (currentStep) {
            1 -> {
                StoreRegisterStep1(
                    selectedCategory = selectedCategory,
                    onCategorySelect = {
                        selectedCategory = it
                    },
                    onNext = {
                        if (selectedCategory != null) {
                            currentStep = 2
                        }
                    },
                )
            }

            2 -> {
                StoreRegisterStep2(
                    categoryLabel = selectedCategory?.label ?: "",
                    categoryIcon = selectedCategory?.icon ?: "🏪",
                    storeName = storeName,
                    onStoreNameChange = {
                        storeName = it
                    },
                    storeAddress = storeAddress,
                    onStoreAddressChange = {
                        storeAddress = it
                    },
                    storeAddressDetail = storeAddressDetail,
                    onStoreAddressDetailChange = {
                        storeAddressDetail = it
                    },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = {
                        phoneNumber = it
                    },
                    ownerName = ownerName,
                    onOwnerNameChange = {
                        ownerName = it
                    },
                    selectedDays = selectedDays,
                    onDayToggle = { day ->
                        selectedDays =
                            if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                    },
                    openTime = openTime,
                    onOpenTimeChange = {
                        openTime = formatTimeInput(it)
                    },
                    closeTime = closeTime,
                    onCloseTimeChange = {
                        closeTime = formatTimeInput(it)
                    },
                    storeCode = storeCode,
                    onGenerateCode = {
                        storeCode =
                            "#${Random.nextInt(10000000, 99999999)}"
                    },
                    storeDescription = storeDescription,
                    onStoreDescriptionChange = {
                        storeDescription = it
                    },
                    imageSelected = imageSelected,
                    onImagePick = {
                        imageSelected = true
                    },
                    onSubmit = {
                        if (
                            storeName.isNotBlank() &&
                            ownerName.isNotBlank() &&
                            phoneNumber.isNotBlank()
                        ) {
                            currentStep = 3
                        }
                    },
                )
            }

            3 -> {
                StoreRegisterStep3(
                    onGoHome = onBackClick,
                )
            }
        }
    }
}

@Composable
private fun StoreRegisterStep1(
    selectedCategory: StoreCategory?,
    onCategorySelect: (StoreCategory) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            ),
    ) {
        Spacer(
            modifier = Modifier.height(100.dp)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEDF7FF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🏪",
                fontSize = 24.sp,
                color = Primary,
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "상점 카테고리를 선택하세요",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "운영하실 상점의 유형을 선택해주세요.",
            fontFamily = Pretendard,
            fontSize = 12.sp,
            color = Color(0xFF595959),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        StoreCategory.entries.forEach { category ->
            val isSelected =
                selectedCategory == category

            StoreCategoryItem(
                category = category,
                isSelected = isSelected,
                onClick = {
                    onCategorySelect(category)
                },
            )

            if (category != StoreCategory.entries.last()) {
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        GradientButton(
            text = "다음 단계로 가기",
            onClick = onNext,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun StoreCategoryItem(
    category: StoreCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
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
                        .border(
                            2.dp,
                            Primary,
                            RoundedCornerShape(15.dp),
                        )
                } else {
                    Modifier.background(Color(0xFFF8F8F8))
                }
            )
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier.width(22.dp)
        )

        Text(
            text = category.icon,
            fontSize = 23.sp,
            color =
                if (isSelected) {
                    Primary
                } else {
                    Color(0xFF121212)
                },
        )

        Spacer(
            modifier = Modifier.width(21.dp)
        )

        Text(
            text = category.label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color =
                if (isSelected) {
                    Primary
                } else {
                    Color(0xFF121212)
                },
        )
    }
}

private fun formatTimeInput(
    input: String,
): String {
    val digits =
        input
            .filter {
                it.isDigit()
            }
            .take(4)

    return if (digits.length <= 2) {
        digits
    } else {
        digits.substring(0, 2) +
                ":" +
                digits.substring(2)
    }
}

@Composable
private fun StoreRegisterStep2(
    categoryLabel: String,
    categoryIcon: String,
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
    imageSelected: Boolean,
    onImagePick: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            ),
    ) {
        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFEDF7FF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = categoryIcon,
                fontSize = 34.sp,
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "선택하신 ${categoryLabel}의 정보를 입력하세요",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "등록하실 상점의 상세 정보를 입력해주세요.",
            fontFamily = Pretendard,
            fontSize = 12.sp,
            color = Color(0xFF595959),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        FormLabel(
            label = "대표 이미지",
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(229.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFF8F8F8))
                .clickable {
                    onImagePick()
                },
            contentAlignment = Alignment.Center,
        ) {
            if (imageSelected) {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "이미지가 선택되었습니다.",
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        color = Color(0xFF595959),
                    )
                }
            } else {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "＋",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "이미지를 업로드해주세요",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFFBFBFBF),
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        FormLabel(
            label = "상점명",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        FormTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            placeholder = "대구광역시 카페",
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "상점 주소",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        FormTextField(
            value = storeAddress,
            onValueChange = onStoreAddressChange,
            placeholder = "대구광역시 수성구 대구광역시 2로 33",
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        FormTextField(
            value = storeAddressDetail,
            onValueChange = onStoreAddressDetailChange,
            placeholder = "시청대구광역시 302호",
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "전화 번호",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        FormTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = "053-123-4567",
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "영업 요일",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        val days = listOf(
            "월",
            "화",
            "수",
            "목",
            "금",
            "토",
            "일",
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            horizontalArrangement =
                Arrangement.spacedBy(4.dp),
        ) {
            days.forEach { day ->
                val selected =
                    selectedDays.contains(day)

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            if (selected) {
                                Color(0xFFEDF7FF)
                            } else {
                                Color(0xFFF8F8F8)
                            }
                        )
                        .border(
                            if (selected) 1.dp else 0.dp,
                            Primary,
                            RoundedCornerShape(13.dp),
                        )
                        .clickable {
                            onDayToggle(day)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color =
                            if (selected) {
                                Primary
                            } else {
                                Color(0xFF8F8F8F)
                            },
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "영업 시간",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            SimpleTimeField(
                value = openTime,
                onValueChange = onOpenTimeChange,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "~",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                modifier = Modifier.padding(
                    horizontal = 12.dp
                ),
            )

            SimpleTimeField(
                value = closeTime,
                onValueChange = onCloseTimeChange,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "대표자 이름",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        FormTextField(
            value = ownerName,
            onValueChange = onOwnerNameChange,
            placeholder = "홍길동",
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "상점 고유 코드",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp),
            horizontalArrangement =
                Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color(0xFFE9E9E9),
                        RoundedCornerShape(8.dp),
                    ),
                contentAlignment =
                    Alignment.CenterStart,
            ) {
                Text(
                    text = if (storeCode.isBlank()) {
                        "#"
                    } else {
                        storeCode
                    },
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color =
                        if (storeCode.isBlank()) {
                            Color(0xFFBFBFBF)
                        } else {
                            Color(0xFF121212)
                        },
                    modifier = Modifier.padding(
                        horizontal = 10.dp
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .width(81.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDF7FF))
                    .clickable {
                        onGenerateCode()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "생성",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Primary,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "상점 고유 코드는 생성버튼을 클릭하면 자동 생성됩니다.",
            fontFamily = Pretendard,
            fontSize = 14.sp,
            color = Color(0xFF8F8F8F),
            modifier = Modifier.padding(
                horizontal = 21.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        FormLabel(
            label = "상점 설명",
            required = true,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 21.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    Color(0xFFE9E9E9),
                    RoundedCornerShape(8.dp),
                ),
        ) {
            BasicTextField(
                value = storeDescription,
                onValueChange = onStoreDescriptionChange,
                textStyle = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 15.sp,
                    color = Color(0xFF121212),
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            )

            if (storeDescription.isEmpty()) {
                Text(
                    text = "상점에 대한 간단한 소개해주세요.",
                    fontFamily = Pretendard,
                    fontSize = 15.sp,
                    color = Color(0xFFBFBFBF),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFFF8F8F8))
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "등록 신청 후 관리자가 검토를 거쳐 승인합니다.\n승인 완료 시 알림을 보내드립니다.",
                fontFamily = Pretendard,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                color = Color(0xFF8F8F8F),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        GradientButton(
            text = "등록 신청하기",
            onClick = onSubmit,
            modifier = Modifier.padding(
                horizontal = 20.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )
    }
}

@Composable
private fun SimpleTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                Color(0xFFE9E9E9),
                RoundedCornerShape(8.dp),
            ),
        contentAlignment =
            Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            singleLine = true,
        )
    }
}

@Composable
private fun StoreRegisterStep3(
    onGoHome: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFEDF7FF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "정상적으로 등록 신청되었습니다.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "관리자가 승인 후 정식 등록됩니다.\n승인 완료 시 알림으로 알려드립니다.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF595959),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        GradientButton(
            text = "홈으로 돌아가기",
            onClick = onGoHome,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
        )
    }
}

@Composable
private fun FormLabel(
    label: String,
    required: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(
            horizontal = 20.dp
        ),
        verticalAlignment =
            Alignment.CenterVertically,
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
            .border(
                1.dp,
                Color(0xFFE9E9E9),
                RoundedCornerShape(8.dp),
            ),
        contentAlignment =
            Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
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
                fontSize = 15.sp,
                color = Color(0xFFBFBFBF),
                modifier = Modifier.padding(
                    horizontal = 10.dp
                ),
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
            .clickable {
                onClick()
            },
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