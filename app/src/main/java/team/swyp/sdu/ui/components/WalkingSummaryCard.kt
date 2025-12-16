package team.swyp.sdu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.ui.theme.Pretendard
import team.swyp.sdu.ui.theme.TypeScale
import java.text.DecimalFormat

/**
 * 산책 기록 요약 카드 컴포넌트
 *
 *
 * - 왼쪽: 누적 걸음 수
 * - 오른쪽: 함께 걸은 기간
 *
 * @param session 산책 세션 데이터
 * @param modifier Modifier
 * @param onClick 클릭 이벤트 핸들러 (선택사항)
 */
@Composable
fun WalkingSummaryCard(
    session: WalkingSession,
    modifier: Modifier = Modifier,
    leftLabel : String,
    rightLabel : String,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x0F000000), // Shadow-1: rgba(0,0,0,0.06) with spread 7
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF), // color/background/whtie-primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // shadow로 처리
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽 섹션: 누적 걸음 수
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // 라벨: "누적 걸음 수"
                Text(
                    text = leftLabel,
                    fontFamily = Pretendard,
                    fontSize = TypeScale.CaptionM, // 12sp
                    fontWeight = FontWeight.Normal, // Regular
                    lineHeight = (TypeScale.CaptionM.value * 1.3f).sp, // lineHeight 1.3
                    letterSpacing = (-0.12f).sp, // letterSpacing -0.12px
                    color = Color(0xFF191919), // color/text-border/primary
                )

                // 걸음 수 + 단위
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 큰 숫자
                    Text(
                        text = formatStepCount(session.stepCount),
                        fontFamily = Pretendard,
                        fontSize = TypeScale.HeadingS, // 22sp
                        fontWeight = FontWeight.Medium, // Medium
                        lineHeight = (TypeScale.HeadingS.value * 1.5f).sp, // lineHeight 1.5
                        letterSpacing = (-0.22f).sp, // letterSpacing -0.22px
                        color = Color(0xFF191919), // color/text-border/primary
                    )

                    // 단위: "걸음"
                    Text(
                        text = "걸음",
                        fontFamily = Pretendard,
                        fontSize = TypeScale.BodyM, // 16sp
                        fontWeight = FontWeight.Normal, // Regular
                        lineHeight = (TypeScale.BodyM.value * 1.5f).sp, // lineHeight 1.5
                        letterSpacing = (-0.16f).sp, // letterSpacing -0.16px
                        color = Color(0xFF191919), // color/text-border/primary
                    )
                }
            }

            // 세로 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color(0xFFF3F3F5)), // color/text-border/secondary-inverse
            )

            // 오른쪽 섹션: 함께 걸은 기간
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // 라벨: "함께 걸은 기간"
                Text(
                    text = rightLabel,
                    fontFamily = Pretendard,
                    fontSize = TypeScale.CaptionM, // 12sp
                    fontWeight = FontWeight.Normal, // Regular
                    lineHeight = (TypeScale.CaptionM.value * 1.3f).sp, // lineHeight 1.3
                    letterSpacing = (-0.12f).sp, // letterSpacing -0.12px
                    color = Color(0xFF191919), // color/text-border/primary
                )

                // 시간 표시: "-시간 -분" 형식
                Text(
                    text = formatDuration(session.duration),
                    fontFamily = Pretendard,
                    fontSize = TypeScale.HeadingS, // 22sp
                    fontWeight = FontWeight.Medium, // Medium
                    lineHeight = (TypeScale.HeadingS.value * 1.5f).sp, // lineHeight 1.5
                    letterSpacing = (-0.22f).sp, // letterSpacing -0.22px
                    color = Color(0xFF191919), // color/text-border/primary
                )
            }
        }
    }
}

/**
 * 걸음 수를 포맷팅 (천 단위 구분자 추가)
 * 예: 18312 -> "18,312"
 */
private fun formatStepCount(stepCount: Int): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(stepCount)
}

/**
 * 시간을 "-시간 -분" 형식으로 포맷팅
 * 예: 3660000ms (61분) -> "1시간 1분"
 * 예: 1800000ms (30분) -> "0시간 30분"
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return if (hours > 0) {
        "${hours}시간 ${minutes}분"
    } else {
        "0시간 ${minutes}분"
    }
}

