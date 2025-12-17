package team.swyp.sdu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import team.swyp.sdu.ui.theme.Pretendard
import team.swyp.sdu.ui.theme.TypeScale
import java.text.DecimalFormat

/**
 * 범용 요약 카드 컴포넌트
 *
 * 두 개의 값을 나란히 표시하는 카드입니다.
 * 산책 기록, 마이페이지 통계 등 다양한 용도로 사용 가능합니다.
 *
 * @param leftLabel 왼쪽 섹션 라벨
 * @param leftValue 왼쪽 섹션 값 (포맷된 문자열)
 * @param leftUnit 왼쪽 섹션 단위 (선택사항, 예: "걸음", "km")
 * @param rightLabel 오른쪽 섹션 라벨
 * @param rightValue 오른쪽 섹션 값 (포맷된 문자열)
 * @param rightUnit 오른쪽 섹션 단위 (선택사항)
 * @param modifier Modifier
 * @param onClick 클릭 이벤트 핸들러 (선택사항)
 */
@Composable
fun WalkingSummaryCard(
    leftLabel: String,
    leftValue: String,
    leftUnit: String? = null,
    rightLabel: String,
    rightValue: String,
    rightUnit: String? = null,
    modifier: Modifier = Modifier,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽 섹션
            SummarySection(
                label = leftLabel,
                value = leftValue,
                unit = leftUnit,
            )

            // 세로 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color(0xFFF3F3F5)), // color/text-border/secondary-inverse
            )

            // 오른쪽 섹션
            SummarySection(
                label = rightLabel,
                value = rightValue,
                unit = rightUnit,
            )
        }
    }
}

/**
 * 요약 섹션 (라벨 + 값 + 단위)
 */
@Composable
private fun RowScope.SummarySection(
    label: String,
    value: String,
    unit: String?,
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // 라벨
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = TypeScale.CaptionM, // 12sp
            fontWeight = FontWeight.Normal, // Regular
            lineHeight = (TypeScale.CaptionM.value * 1.3f).sp, // lineHeight 1.3
            letterSpacing = (-0.12f).sp, // letterSpacing -0.12px
            color = Color(0xFF191919), // color/text-border/primary
        )

        // 값 + 단위
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 값
            Text(
                text = value,
                fontFamily = Pretendard,
                fontSize = TypeScale.HeadingS, // 22sp
                fontWeight = FontWeight.Medium, // Medium
                lineHeight = (TypeScale.HeadingS.value * 1.5f).sp, // lineHeight 1.5
                letterSpacing = (-0.22f).sp, // letterSpacing -0.22px
                color = Color(0xFF191919), // color/text-border/primary
            )

            // 단위 (있는 경우만 표시)
            unit?.let {
                Text(
                    text = it,
                    fontFamily = Pretendard,
                    fontSize = TypeScale.BodyM, // 16sp
                    fontWeight = FontWeight.Normal, // Regular
                    lineHeight = (TypeScale.BodyM.value * 1.5f).sp, // lineHeight 1.5
                    letterSpacing = (-0.16f).sp, // letterSpacing -0.16px
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
fun formatStepCount(stepCount: Int): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(stepCount)
}

/**
 * 시간을 "-시간 -분" 형식으로 포맷팅
 * 예: 3660000ms (61분) -> "1시간 1분"
 * 예: 1800000ms (30분) -> "0시간 30분"
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return if (hours > 0) {
        "${hours}시간 ${minutes}분"
    } else {
        "0시간 ${minutes}분"
    }
}

/**
 * 거리를 포맷팅 (km 또는 m)
 * 예: 5200.0m -> "5.2"
 * 예: 500.0m -> "500"
 */
fun formatDistance(distanceMeters: Float): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1f", distanceMeters / 1000f)
    } else {
        DecimalFormat("#,###").format(distanceMeters.toInt())
    }
}

