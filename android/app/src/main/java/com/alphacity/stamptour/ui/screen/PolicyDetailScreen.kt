package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.viewmodel.TermsViewModel

@Composable
fun PolicyDetailScreen(
    policyType: PolicyType,
    onBackClick: () -> Unit,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val apiType = when (policyType) {
        PolicyType.TERMS -> "service"
        PolicyType.PRIVACY -> "privacy"
        PolicyType.LOCATION -> "location"
    }

    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(apiType) {
        viewModel.fetchTerms(apiType)
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
                    .clickable(onClick = onBackClick),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = policyType.title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // === Content ===
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                if (content.isNotBlank()) {
                    // API content
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF121212),
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = content,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        letterSpacing = (-0.2).sp,
                        color = Color(0xFF595959),
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                } else {
                    // Fallback to hardcoded content
                    val sections = getPolicySections(policyType)
                    sections.forEach { section ->
                        PolicySection(section)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Effective date notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "본 약관은 2026년 2월 2일부터 시행됩니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer
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

@Composable
private fun PolicySection(section: PolicySectionData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
    ) {
        Text(
            text = section.title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFF121212),
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = section.content,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = (-0.2).sp,
            color = Color(0xFF595959),
        )
    }
}

private data class PolicySectionData(
    val title: String,
    val content: String,
)

private fun getPolicySections(type: PolicyType): List<PolicySectionData> = when (type) {
    PolicyType.TERMS -> listOf(
        PolicySectionData(
            "제 1조(목적)",
            "본 약관은 디지털 페스티벌 앱(이하 \"서비스\")의 이용과 관련하여 회사와 이용자 간의 권리,\n의무 및 책임사항을 규정함을 목적으로 합니다."
        ),
        PolicySectionData(
            "제 2조 (정의)",
            "1. \"서비스\"란 디지털 페스티벌 관련 정보 제공, 미션 참여, 스탬프 수집 등의 기능을 제공하는\n모바일 애플리케이션을 의미합니다.\n2. \"이용자\"란 본 약관에 따라 서비스를 이용하는 회원 및 비회원을 말합니다.\n3. \"회원\"이란 서비스에 가입하여 지속적으로 서비스를 이용할 수 있는 자를 말합니다."
        ),
        PolicySectionData(
            "제 3조 (서비스의 제공)",
            "1. 서비스는 연중무휴, 1일 24시간 제공함을 원칙으로 합니다.\n2. 회사는 컴퓨터 등 정보통신설비의 보수점검, 교체 및 고장, 통신두절 또는 운영상 상당한\n이유가 있는 경우 서비스의 제공을 일시적으로 중단할 수 있습니다."
        ),
        PolicySectionData(
            "제 4조 (이용자의 의무)",
            "1. 이용자는 관계법령, 본 약관의 규정, 이용안내 및 서비스상에 공지한 주의사항, 회사가 통지하는\n사항 등을 준수하여야 합니다.\n2. 이용자는 서비스 이용 시 타인의 권리나 명예, 신용 기타 정당한 이익을 침해하여서는\n안 됩니다."
        ),
        PolicySectionData(
            "제 5조 (개인정보 보호)",
            "회사는 이용자의 개인정보를 보호하기 위하여 개인정보처리방침을 수립하고 이를 준수합니다.\n자세한 내용은 별도의 개인정보 처리방침을 참고하시기 바랍니다."
        ),
    )

    PolicyType.PRIVACY -> listOf(
        PolicySectionData(
            "1. 개인정보의 수집 및 이용 목적",
            "회사는 다음의 목적을 위해 개인정보를 수집하고 이용합니다.\n- 회원 가입 및 관리\n- 서비스 제공 및 개선\n- 이벤트 참여 및 미션 진행\n- 고객 문의 응대 및 불만 처리\n- 서비스 관련 공지사항 전달"
        ),
        PolicySectionData(
            "2. 수집하는 개인정보 항목",
            "회사는 회원가입, 서비스 이용 등을 위해 아래와 같은 개인정보를 수집하고 있습니다.\n- 필수 항목 : 이메일 주소, 닉네임, 전화번호\n- 선택 항목 : 주소, 위치 정보(미션 참여 시)"
        ),
        PolicySectionData(
            "3. 개인정보의 보유 및 이용 기간",
            "회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시 동의받은\n개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.\n- 회원 탈퇴 시까지 (단, 관계 법령에 따라 보존할 필요가 있는 경우 해당 기간 동안 보관)"
        ),
        PolicySectionData(
            "4. 개인정보의 제3자 제공",
            "회사는 원칙적으로 이용자의 개인정보를 제3자에게 제공하지 않습니다. 다만, 이용자의 동의가\n있거나 법령의 규정에 의한 경우는 예외로 합니다."
        ),
        PolicySectionData(
            "5. 정보주체의 권리·의무 및 행사방법",
            "정보주체는 회사에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.\n- 개인정보 열람 요구\n- 오류 등이 있을 경우 정정 요구\n- 삭제 요구\n- 처리 정지 요구"
        ),
        PolicySectionData(
            "6. 개인정보의 안전성 확보조치",
            "회사는 개인정보의 안전성 확보를 위해 다음과 같은 조치를 취하고 있습니다.\n- 개인정보 암호화\n- 해킹 등에 대비한 기술적 대책\n- 개인정보에 대한 접근 제한"
        ),
    )

    PolicyType.LOCATION -> listOf(
        PolicySectionData(
            "제 1조 (목적)",
            "본 약관은 디지털 페스티벌 앱(이하 \"회사\")이 제공하는 위치기반서비스(이하 \"서비스\")와\n관련하여 회사와 개인위치정보주체(이하 \"이용자\") 상호간의 권리, 의무 및 책임사항, 기타\n필요한 사항을 규정함을 목적으로 합니다."
        ),
        PolicySectionData(
            "제 2조 (이용약관의 효력 및 변경)",
            "1. 본 약관은 이용자가 본 약관에 동의하고 회사가 정한 절차에 따라 서비스의 이용자로\n등록됨으로써 효력이 발생합니다.\n2. 회사는 법률이나 서비스의 변경사항을 반영하기 위한 목적 등으로 약관을 수정할 수 있습니다."
        ),
        PolicySectionData(
            "제 3조 (위치정보의 수집 및 이용)",
            "회사는 다음과 같은 목적으로 이용자의 위치정보를 수집·이용합니다.\n- 위치 기반 미션 참여 및 인증\n- 주변 시설 및 프로그램 정보 제공\n- 지도 기반 서비스 제공\n- 이벤트 참여 위치 확인\n\n수집된 위치정보는 해당 목적으로만 사용되며, 목적 달성 후 즉시 파기됩니다."
        ),
        PolicySectionData(
            "제 4조 (위치정보의 보호)",
            "1. 회사는 위치정보의 수집·이용·제공 시 이용자의 사생활을 침해하지 않도록 주의를 기울입니다.\n2. 회사는 위치정보를 안전하게 관리하기 위하여 필요한 기술적·관리적 조치를 취합니다.\n3. 회사는 이용자의 동의 없이 위치정보를 제3자에게 제공하지 않습니다."
        ),
        PolicySectionData(
            "제 5조 (이용자의 권리)",
            "1. 이용자는 언제든지 위치정보의 수집·이용·제공에 대한 동의를 철회할 수 있습니다.\n2. 이용자는 언제든지 자신의 위치정보 수집·이용·제공 사실 확인 자료의 열람 또는 고지를\n요구할 수 있습니다.\n3. 이용자는 위치정보 수집·이용·제공 목적, 제공받는 자의 범위 및 위치기반서비스의 일부에\n대하여 동의를 유보할 수 있습니다."
        ),
        PolicySectionData(
            "제 6조 (위치정보 관리책임자)",
            "회사는 위치정보를 적절히 관리·보호하고 이용자의 불만을 원활히 처리할 수 있도록 위치정보\n관리책임자를 지정하여 운영하고 있습니다.\n\n담당자: 고객지원팀\n이메일: support@digitalfestival.com"
        ),
    )
}
