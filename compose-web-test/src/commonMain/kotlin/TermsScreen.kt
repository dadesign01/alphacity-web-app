package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.TermItem
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import org.jetbrains.compose.resources.painterResource

@Composable
fun TermsScreen(
    onBackClick: () -> Unit = {},
) {
    var terms by remember {
        mutableStateOf<List<TermItem>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        terms = emptyList()

        try {
            // 휴대폰 인증 시 동의하는 약관
            val response = ApiService.getTerms("phone_verification")

            if (response.success) {
                terms = response.data ?: emptyList()

                if (terms.isEmpty()) {
                    errorMessage = "등록된 약관 내용이 없습니다."
                }
            } else {
                errorMessage = response.error?.message
                    ?: "약관 내용을 불러오지 못했습니다."
            }
        } catch (e: Exception) {
            println(
                "휴대폰 인증 약관 조회 오류: ${e.message}"
            )

            errorMessage = "약관 내용을 불러오는 중 오류가 발생했습니다."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
    ) {

        // ========================================
        // 헤더
        // PhoneLogin과 동일한 구조
        // ========================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    Res.drawable.header_left_arrow
                ),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(
                        width = 8.dp,
                        height = 15.dp,
                    )
                    .clickable {
                        onBackClick()
                    },
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "이용약관 및 개인정보 처리방침 안내",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121212),
            )
        }

        // ========================================
        // 헤더 하단 border
        // ========================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFB5B5B5)),
        )

        // ========================================
        // 약관 내용
        // ========================================

        when {

            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF2563EB),
                    )
                }
            }

            terms.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = errorMessage
                            ?: "약관 내용을 불러오지 못했습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(
                                rememberScrollState()
                            ),
                    ) {
                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                        terms.forEach { term ->
                            PolicySection(
                                term = term,
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )
                    }

                    // ========================================
                    // 시행일
                    // ========================================

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(99.dp)
                            .background(
                                Color(0xFFDEDEDE)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "본 약관은 2026년 09월 04일부터 시행됩니다.",
                            fontSize = 14.sp,
                            color = Color(0xFF5E5E5E),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    term: TermItem,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = term.title,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF121212),
        )

        Text(
            text = term.content,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = Color(0xFF595959),
        )
    }
}