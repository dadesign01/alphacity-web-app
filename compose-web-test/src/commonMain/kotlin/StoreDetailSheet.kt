import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBg = Color(0xFF1B2038)
private val CardBg = Color(0xFF252B45)
private val Pretendard = FontFamily.SansSerif

@Composable
fun StoreDetailSheet(
    program: Any,
    onDismiss: () -> Unit,
) {
    var isSpeaking by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                DarkBg,
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(20.dp),
        ) {
            // Category + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text = "맛집",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(
                        alpha = 0.8f
                    ),
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            Color.White.copy(
                                alpha = 0.15f
                            )
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 4.dp,
                        ),
                )

                Text(
                    text = "×",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            onDismiss()
                        },
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // Store name
            Text(
                text = "상점 상세",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(CardBg),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = "🏪",
                    fontSize = 54.sp,
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Description + TTS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(CardBg)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.Top,
                ) {
                    Text(
                        text = "상점 소개입니다. 방문객에게 다양한 상품과 서비스를 제공합니다.",
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color.White.copy(
                            alpha = 0.85f
                        ),
                        modifier =
                            Modifier.weight(1f),
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(
                        text =
                            if (isSpeaking) {
                                "🔊"
                            } else {
                                "🔈"
                            },
                        fontSize = 20.sp,
                        color =
                            Color.White.copy(
                                alpha = 0.7f
                            ),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                isSpeaking =
                                    !isSpeaking
                            },
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        )
                    )
                    .padding(vertical = 14.dp),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = "미션이 등록되지 않은 상점입니다.",
                    fontFamily = Pretendard,
                    fontWeight =
                        FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White.copy(
                        alpha = 0.5f
                    ),
                )
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}