package com.alphacity.stamptour.ui.screen

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import java.util.Locale

private val DarkBg = Color(0xFF1B2038)
private val CardBg = Color(0xFF252B45)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailSheet(
    program: ProgramItem,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { isSpeaking = false }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { isSpeaking = false }
                })
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            tts?.stop()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkBg,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // Category tag + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "맛집",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            tts?.stop()
                            onDismiss()
                        },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Store name
            Text(
                text = program.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Store image
            val imageUrl = program.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                val fullUrl = if (imageUrl.startsWith("http")) imageUrl
                    else BuildConfig.SERVER_URL + imageUrl
                AsyncImage(
                    model = fullUrl,
                    contentDescription = program.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = program.name.take(1),
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        color = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description card with TTS
            if (!program.description.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg)
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = program.description,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f),
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop
                                else Icons.Outlined.VolumeUp,
                            contentDescription = if (isSpeaking) "TTS 중지" else "TTS 재생",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    if (isSpeaking) {
                                        tts?.stop()
                                        isSpeaking = false
                                    } else {
                                        tts?.speak(
                                            program.description,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "store_desc",
                                        )
                                        isSpeaking = true
                                    }
                                },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "미션이 등록되지 않은 상점입니다" message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "미션이 등록되지 않은 상점입니다",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
