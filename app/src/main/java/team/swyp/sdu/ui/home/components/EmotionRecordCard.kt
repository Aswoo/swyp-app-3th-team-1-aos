package team.swyp.sdu.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import team.swyp.sdu.data.model.EmotionType
import team.swyp.sdu.ui.home.WeeklyEmotionSummary

/**
 * 나의 감정 기록 카드
 * 이번 주 주요 감정을 표시하는 컴포넌트
 */
@Composable
fun EmotionRecordCard(
    emotionSummary: WeeklyEmotionSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "이번주 나의 주요 감정은?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                )
                Text(
                    text = getEmotionDisplayName(emotionSummary.mainEmotion),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "${getEmotionDisplayName(emotionSummary.mainEmotion)} 감정을 ${emotionSummary.totalDays}일동안 ${emotionSummary.mainEmotionCount}회 경험했어요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                )
                Text(
                    text = "남은 일상도 워킷과 함께 즐겁게 보내볼까요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                )
            }

            // 웃는 얼굴 이모지
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = getEmotionEmoji(emotionSummary.mainEmotion),
                    style = MaterialTheme.typography.displayMedium,
                )
            }
        }
    }
}

/**
 * 감정 타입을 한글 이름으로 변환
 */
private fun getEmotionDisplayName(emotionType: EmotionType): String {
    return when (emotionType) {
        EmotionType.HAPPY -> "기쁨"
        EmotionType.JOYFUL -> "즐거움"
        EmotionType.CONTENT -> "행복함"
        EmotionType.DEPRESSED -> "우울함"
        EmotionType.TIRED -> "지침"
        EmotionType.ANXIOUS -> "짜증남"
    }
}

/**
 * 감정 타입에 맞는 이모지 반환
 */
private fun getEmotionEmoji(emotionType: EmotionType): String {
    return when (emotionType) {
        EmotionType.HAPPY,
        EmotionType.JOYFUL,
        EmotionType.CONTENT -> "😊"
        EmotionType.DEPRESSED -> "😢"
        EmotionType.TIRED -> "😴"
        EmotionType.ANXIOUS -> "😐"
    }
}

