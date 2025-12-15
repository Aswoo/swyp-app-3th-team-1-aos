package team.swyp.sdu.ui.walking

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.presentation.viewmodel.WalkingViewModel
import team.swyp.sdu.ui.walking.components.EmotionProgressIndicator

/**
 * 감정 선택 단계 화면 (단계 1)
 * 슬라이더로 감정을 선택하는 화면
 */
@Composable
fun EmotionSelectionStep(
    viewModel: WalkingViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val emotionValue by viewModel.emotionValue.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 오른쪽 상단 닫기 버튼
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
            )
        }

        // 중앙 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // 진행률 표시기
            EmotionProgressIndicator(
                currentStep = 1,
                totalSteps = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 제목
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "산책 종료",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "산책 후 감정 기록하기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 감정 선택 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    // 캐릭터 표정
                    EmotionCharacter(emotionValue = emotionValue)

                    // 안내 문구
                    Text(
                        text = "산책 후 감정을 기록해주세요!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                    )

                    // 감정 슬라이더
                    Slider(
                        value = emotionValue,
                        onValueChange = { viewModel.setEmotionValue(it) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = Color(0xFFE5E5E5),
                            activeTrackColor = Color(0xFF2E2E2E),
                            inactiveTrackColor = Color(0xFFE5E5E5),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 다음 버튼
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E2E2E),
                ),
            ) {
                Text(
                    text = "다음",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * 감정 값에 따라 표정이 변하는 캐릭터
 */
@Composable
private fun EmotionCharacter(
    emotionValue: Float,
    modifier: Modifier = Modifier,
) {
    val animatedValue by animateFloatAsState(
        targetValue = emotionValue,
        animationSpec = tween(durationMillis = 300),
        label = "emotion",
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEmotionFace(animatedValue)
        }
    }
}

/**
 * 감정 값에 따라 얼굴을 그립니다
 * 0.0: 매우 부정적 (슬픔)
 * 0.5: 중립
 * 1.0: 매우 긍정적 (행복)
 */
private fun DrawScope.drawEmotionFace(emotionValue: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.minDimension / 3f

    // 얼굴 배경 (원)
    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(centerX, centerY),
    )

    // 눈 (항상 동일)
    val eyeRadius = radius * 0.15f
    val eyeY = centerY - radius * 0.2f
    val eyeSpacing = radius * 0.4f

    // 왼쪽 눈
    drawCircle(
        color = Color.Black,
        radius = eyeRadius,
        center = Offset(centerX - eyeSpacing, eyeY),
    )

    // 오른쪽 눈
    drawCircle(
        color = Color.Black,
        radius = eyeRadius,
        center = Offset(centerX + eyeSpacing, eyeY),
    )

    // 코 (작은 빨간 원)
    val noseRadius = radius * 0.08f
    drawCircle(
        color = Color(0xFFFF6B6B),
        radius = noseRadius,
        center = Offset(centerX, centerY),
    )

    // 입 (감정에 따라 변화)
    val mouthY = centerY + radius * 0.3f
    val mouthWidth = radius * 0.6f
    val mouthHeight = radius * 0.2f * (if (emotionValue > 0.5f) 1f else -1f)

    val mouthPath = Path().apply {
        if (emotionValue > 0.5f) {
            // 긍정적: 위로 올라간 곡선 (미소)
            moveTo(centerX - mouthWidth / 2f, mouthY)
            quadraticBezierTo(
                x1 = centerX,
                y1 = mouthY - mouthHeight,
                x2 = centerX + mouthWidth / 2f,
                y2 = mouthY,
            )
        } else {
            // 부정적: 아래로 내려간 곡선 (슬픔)
            moveTo(centerX - mouthWidth / 2f, mouthY)
            quadraticBezierTo(
                x1 = centerX,
                y1 = mouthY + mouthHeight,
                x2 = centerX + mouthWidth / 2f,
                y2 = mouthY,
            )
        }
    }

    drawPath(
        path = mouthPath,
        color = Color(0xFF4ECDC4),
        style = Stroke(
            width = 8f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        ),
    )
}

