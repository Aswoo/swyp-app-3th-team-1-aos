package team.swyp.sdu.ui.record.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import team.swyp.sdu.presentation.viewmodel.CalendarViewModel.WalkAggregate
import team.swyp.sdu.data.model.WalkingSession
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 헤더 행 컴포넌트
 */
@Composable
fun HeaderRow(
    onDummyClick: () -> Unit,
    onStartOnboarding: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "기록",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Row {
            Button(
                onClick = onDummyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                ),
            ) {
                Text("더미 데이터")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onStartOnboarding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                ),
            ) {
                Text("온보딩 시작")
            }
        }
    }
}

/**
 * 월간 섹션 컴포넌트
 */
@Composable
fun MonthSection(
    stats: WalkAggregate,
    sessions: List<WalkingSession>,
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val emotionsByDate = remember(sessions) {
        sessions.flatMap { session ->
            session.emotions.map { emotion ->
                val date = java.time.Instant.ofEpochMilli(emotion.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                date to emotion
            }
        }.groupBy({ it.first }, { it.second })
    }

    val sessionsByDate = remember(sessions) {
        sessions.groupBy { session ->
            java.time.Instant.ofEpochMilli(session.startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    val monthlyStats = remember(sessions, currentMonth) {
        calculateMonthlyStatsForRecord(sessions, currentMonth, emotionsByDate)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MonthNavigator(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
        )

        CalendarGridRecord(
            yearMonth = currentMonth,
            emotionsByDate = emotionsByDate,
            sessionsByDate = sessionsByDate,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        EmotionCard(
            title = "이번달 주요 감정",
            emotion = monthlyStats.primaryMood,
            desc = monthlyStats.description,
        )

        StatsRow(
            listOf(
                StatItem("걸음 수", "%,d".format(monthlyStats.totalSteps)),
                StatItem("세션 수", "%,d".format(monthlyStats.sessionsCount)),
                StatItem("포커스", "${monthlyStats.focusScore} 점"),
            ),
        )
    }
}

/**
 * 주간 섹션 컴포넌트
 */
@Composable
fun WeekSection(stats: WalkAggregate) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard {
            Text(text = "주간 캘린더", style = MaterialTheme.typography.titleMedium)
        }

        EmotionCard(
            title = "이번주 나의 주요 감정은?",
            emotion = "즐거움",
            desc = "즐거운 감정을 7일동안 4회 경험했어요!",
        )

        StatsRow(
            listOf(
                StatItem("걸음 수", "%,d".format(stats.steps)),
                StatItem("산책 시간", "${stats.durationHours}시간 ${stats.durationMinutesRemainder}분"),
            ),
        )

        GoalCheckRow()
    }
}

/**
 * 일간 섹션 컴포넌트
 */
@Composable
fun DaySection(
    stats: WalkAggregate,
    sessions: List<WalkingSession>,
    dateLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceAtMost((sessions.size - 1).coerceAtLeast(0)) }
    }
    val totalDistanceMeters by remember(sessions) {
        mutableStateOf(
            sessions.fold(0.0) { acc, session ->
                acc + computeRouteDistanceMeters(session.locations).coerceAtLeast(session.totalDistance.toDouble())
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 날짜 네비게이터
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "이전 날짜")
            }

            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "다음 날짜")
            }
        }

        // 통계 카드들
        StatsRow(
            listOf(
                StatItem("걸음 수", "%,d".format(stats.steps)),
                StatItem("산책 시간", "${stats.durationHours}시간 ${stats.durationMinutesRemainder}분"),
                StatItem("거리", "%.2f km".format(totalDistanceMeters / 1000)),
            ),
        )

        // 세션 목록
        if (sessions.isNotEmpty()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(sessions.size) { index ->
                    val session = sessions[index]
                    SessionItem(
                        session = session,
                        isSelected = index == currentIndex,
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "이 날짜에 산책 기록이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }
        }
    }
}

/**
 * 월 네비게이터 컴포넌트
 */
@Composable
private fun MonthNavigator(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "이전 달")
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "다음 달")
        }
    }
}

/**
 * 캘린더 그리드 컴포넌트
 */
@Composable
private fun CalendarGridRecord(
    yearMonth: YearMonth,
    emotionsByDate: Map<LocalDate, List<team.swyp.sdu.data.model.Emotion>>,
    sessionsByDate: Map<LocalDate, List<WalkingSession>>,
    modifier: Modifier = Modifier,
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        var dayIndex = 0
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                        )
                    } else if (dayIndex < daysInMonth) {
                        val date = yearMonth.atDay(dayIndex + 1)
                        val emotions = emotionsByDate[date] ?: emptyList()
                        val primaryEmotion = emotions.firstOrNull()
                        val hasWalkSession = sessionsByDate[date]?.isNotEmpty() == true

                        CalendarDayCellRecord(
                            day = dayIndex + 1,
                            emotion = primaryEmotion,
                            hasWalkSession = hasWalkSession,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                        )
                        dayIndex++
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 캘린더 데이 셀 컴포넌트
 */
@Composable
private fun CalendarDayCellRecord(
    day: Int,
    emotion: team.swyp.sdu.data.model.Emotion?,
    hasWalkSession: Boolean,
    modifier: Modifier = Modifier,
) {
    // 기본 구현: 날짜 숫자 표시
    val (backgroundColor, _) = getMoodColorAndEmojiRecord(emotion?.type)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = emotion != null) { /* TODO: 날짜 클릭 처리 */ },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (emotion != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )

            // 산책 세션이 있으면 초록색 점 표시
            if (hasWalkSession) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Green 색상
                )
            }
        }
    }
}

/**
 * 감정 카드 컴포넌트
 */
@Composable
fun EmotionCard(
    title: String,
    emotion: String,
    desc: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF97FFB5),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = emotion,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * 통계 행 컴포넌트
 */
@Composable
fun StatsRow(items: List<StatItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * 세션 아이템 컴포넌트
 */
@Composable
private fun SessionItem(
    session: WalkingSession,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "산책 세션",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "걸음 수: ${session.stepCount}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "거리: %.2f km".format(session.totalDistance / 1000),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * 섹션 카드 컴포넌트
 */
@Composable
fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            content()
        }
    }
}

/**
 * 목표 체크 행 컴포넌트
 */
@Composable
fun GoalCheckRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "주간 목표 달성률",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "70% 달성했어요!",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "완료",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/**
 * 통계 아이템 데이터 클래스
 */
data class StatItem(
    val title: String,
    val value: String,
)

/**
 * 월간 통계 데이터 클래스
 */
private data class MonthlyStatsRecord(
    val primaryMood: String,
    val description: String,
    val totalSteps: Int,
    val sessionsCount: Int,
    val focusScore: Int,
)

/**
 * 월간 통계 계산 함수
 */
private fun calculateMonthlyStatsForRecord(
    sessions: List<WalkingSession>,
    month: YearMonth,
    emotionsByDate: Map<LocalDate, List<team.swyp.sdu.data.model.Emotion>>,
): MonthlyStatsRecord {
    val monthStart = month.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val monthEnd = month.atEndOfMonth().atTime(23, 59, 59)
        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val monthSessions = sessions.filter { session ->
        session.startTime in monthStart..monthEnd
    }

    val totalSteps = monthSessions.sumOf { it.stepCount }
    val sessionsCount = monthSessions.size
    val focusScore = if (monthSessions.isNotEmpty()) {
        (sessionsCount * 20).coerceAtMost(100) // 간단한 계산
    } else {
        0
    }

    // 주요 감정 계산 (간단 버전)
    val allEmotions = emotionsByDate.values.flatten()
    val primaryMood = if (allEmotions.isNotEmpty()) {
        allEmotions.first().type.name // 간단하게 첫 번째 감정 사용
    } else {
        "보통"
    }

    return MonthlyStatsRecord(
        primaryMood = primaryMood,
        description = "이번 달의 주요 감정입니다.",
        totalSteps = totalSteps,
        sessionsCount = sessionsCount,
        focusScore = focusScore,
    )
}

/**
 * 감정 타입에 따른 색상과 이모지 반환
 */
fun getMoodColorAndEmojiRecord(emotionType: team.swyp.sdu.data.model.EmotionType?): Pair<Color, String> =
    when (emotionType) {
        team.swyp.sdu.data.model.EmotionType.HAPPY -> Color(0xFFFFF59D) to "😊"
        team.swyp.sdu.data.model.EmotionType.JOYFUL -> Color(0xFFFFD54F) to "🤩"
        team.swyp.sdu.data.model.EmotionType.CONTENT -> Color(0xFF81C784) to "😄"
        team.swyp.sdu.data.model.EmotionType.DEPRESSED -> Color(0xFF90A4AE) to "😔"
        team.swyp.sdu.data.model.EmotionType.TIRED -> Color(0xFFB0BEC5) to "😴"
        team.swyp.sdu.data.model.EmotionType.ANXIOUS -> Color(0xFF80DEEA) to "😰"
        null -> Color.White to "-"
    }

/**
 * 경로 거리 계산 함수 (간단 버전)
 */
private fun computeRouteDistanceMeters(locations: List<team.swyp.sdu.data.model.LocationPoint>): Double {
    if (locations.size < 2) return 0.0

    var totalDistance = 0.0
    for (i in 0 until locations.size - 1) {
        val loc1 = locations[i]
        val loc2 = locations[i + 1]

        val lat1 = loc1.latitude
        val lon1 = loc1.longitude
        val lat2 = loc2.latitude
        val lon2 = loc2.longitude

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        totalDistance += 6371000 * c // 지구 반지름 * c
    }

    return totalDistance
}
