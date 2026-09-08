package com.alphacity.stamptour.ui.screen

import PolicyType
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.TermItem

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun PolicyDetailScreen(
    policyType: PolicyType,
    onBackClick: () -> Unit,
) {
    var terms by remember { mutableStateOf<List<TermItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(policyType) {
        isLoading = true
        errorMessage = null
        terms = emptyList()

        val type = when (policyType) {
            PolicyType.TERMS -> "service"
            PolicyType.PRIVACY -> "privacy"
            PolicyType.LOCATION -> "location"
        }

        try {
            val response = ApiService.getTerms(type)

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
            errorMessage = "약관 내용을 불러오는 중 오류가 발생했습니다."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    onBackClick()
                },
            )

            Spacer(
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = policyType.title,
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

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                    )
                }
            }

            terms.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = errorMessage
                            ?: "약관 내용을 불러오지 못했습니다.",
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(Color(0xFFF8F8F8))
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "약관의 내용은 서비스 정책에 따라 변경될 수 있습니다.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color(0xFF8F8F8F),
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F9F9))
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "2026 OLLYMOA. All rights reserved.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color(0xFF8F8F8F),
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
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF121212),
        )

        Text(
            text = term.content,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = Color(0xFF595959),
        )
    }
}