package team.swyp.sdu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.data.model.Emotion
import team.swyp.sdu.data.model.EmotionType
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.presentation.viewmodel.WalkingSessionListViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 캘린더 화면
 * 기분(Mood) 캘린더를 표시하고 월간 요약 및 통계를 제공합니다.
 */
@Composable
fun CalendarScreen(
    onNavigateToRouteDetail: (List<team.swyp.sdu.data.model.LocationPoint>) -> Unit,
    viewModel: WalkingSessionListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions = when (val state = uiState) {
        is team.swyp.sdu.presentation.viewmodel.WalkingSessionListUiState.Success -> state.sessions
        else -> emptyList()
    }

    // 현재 월 상태
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 날짜별 감정 맵 생성 (세션의 emotions에서 추출)
    val emotionsByDate = remember(sessions) {
        sessions.flatMap { session ->
            session.emotions.map { emotion ->
                val date = java.time.Instant.ofEpochMilli(emotion.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                date to emotion
            }
        }.groupBy({ it.first }, { it.second })
    }

    // 월간 통계 계산
    val monthlyStats = remember(sessions, currentMonth) {
        calculateMonthlyStats(sessions, currentMonth, emotionsByDate)
    }

    val navigationBarsPadding = androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues()

    // 캘린더 화면 시작 시 11월 더미 데이터 생성
    LaunchedEffect(Unit) {
        viewModel.generateNovemberTestData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(navigationBarsPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        // 상단 헤더
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            onSearchClick = { /* TODO: 검색 기능 */ },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 캘린더 그리드
        CalendarGrid(
            yearMonth = currentMonth,
            emotionsByDate = emotionsByDate,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 월간 기분 요약
        MonthlyMoodSummary(
            primaryMood = monthlyStats.primaryMood,
            description = monthlyStats.description,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 통계 카드들
        StatisticsCards(
            totalSteps = monthlyStats.totalSteps,
            sessionsCount = monthlyStats.sessionsCount,
            focusScore = monthlyStats.focusScore,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 캘린더 헤더
 */
@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 뒤로가기 버튼 (이미지에 맞춰 왼쪽에 배치)
        IconButton(
            onClick = { /* TODO: 뒤로가기 처리 - 탭 내부에서는 필요 없을 수 있음 */ },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 제목 및 날짜 (중앙 정렬)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mood Calendar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.ENGLISH),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 검색/필터 아이콘 (오른쪽)
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
    
    // 이전/다음 달 버튼을 헤더 아래에 별도로 배치 (이미지에는 없지만 기능 유지)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "이전 달",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "다음 달",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * 캘린더 그리드
 */
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    emotionsByDate: Map<LocalDate, List<Emotion>>,
    modifier: Modifier = Modifier,
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = 일요일
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = modifier) {
        // 요일 헤더
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
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 날짜 그리드
        var dayIndex = 0
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // 첫 주의 빈 칸
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

                        CalendarDayCell(
                            day = dayIndex + 1,
                            emotion = primaryEmotion,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                        )
                        dayIndex++
                    } else {
                        // 마지막 주의 빈 칸
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
 * 캘린더 날짜 셀
 */
@Composable
private fun CalendarDayCell(
    day: Int,
    emotion: Emotion?,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, emoji) = getMoodColorAndEmoji(emotion?.type)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = emotion != null) { /* TODO: 날짜 클릭 처리 */ },
        contentAlignment = Alignment.Center,
    ) {
        if (emotion != null) {
            Text(
                text = emoji,
                fontSize = 20.sp,
            )
        } else {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * 월간 기분 요약 카드
 */
@Composable
private fun MonthlyMoodSummary(
    primaryMood: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF97FFB5), // 연한 초록색
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Monthly Mood Summary",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32), // 진한 초록색
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = primaryMood,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20), // 더 진한 초록색
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50), // 중간 초록색
                    lineHeight = 22.sp,
                )
            }
            // 큰 이모지 그래픽 (이미지처럼 더 크게)
            Text(
                text = getMoodEmoji(primaryMood),
                fontSize = 72.sp,
            )
        }
    }
}

/**
 * 통계 카드들
 */
@Composable
private fun StatisticsCards(
    totalSteps: Int,
    sessionsCount: Int,
    focusScore: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatisticCard(
            title = "Activity",
            value = formatNumber(totalSteps),
            unit = "Steps",
            modifier = Modifier.weight(1f),
        )
        StatisticCard(
            title = "Therapy",
            value = "$sessionsCount/30",
            unit = "Sessions",
            modifier = Modifier.weight(1f),
        )
        StatisticCard(
            title = "Discipline",
            value = "$focusScore%",
            unit = "Focus score",
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 통계 카드
 */
@Composable
private fun StatisticCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
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
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 감정 타입에 따른 색상과 이모지 반환
 * 이미지의 색상 팔레트에 맞춰 조정
 */
private fun getMoodColorAndEmoji(emotionType: EmotionType?): Pair<Color, String> {
    return when (emotionType) {
        // 새로운 감정 타입들
        EmotionType.HAPPY -> Color(0xFFFFE082) to "😊" // 노란색
        EmotionType.JOYFUL -> Color(0xFFFFE082) to "😄" // 노란색
        EmotionType.LIGHT_FOOTED -> Color(0xFFC5E1A5) to "🚶" // 연한 초록색
        EmotionType.EXCITED -> Color(0xFFFFB74D) to "🤩" // 주황색
        EmotionType.THRILLED -> Color(0xFFFFB74D) to "✨" // 주황색
        EmotionType.TIRED -> Color(0xFFCE93D8) to "😴" // 보라색
        EmotionType.SAD -> Color(0xFFFFAB91) to "😢" // 연한 주황색
        EmotionType.DEPRESSED -> Color(0xFF90CAF9) to "😔" // 연한 파란색
        EmotionType.SLUGGISH -> Color(0xFFB0BEC5) to "😑" // 회색
        EmotionType.MANY_THOUGHTS -> Color(0xFFB39DDB) to "🤔" // 보라색
        EmotionType.COMPLEX_MIND -> Color(0xFFB39DDB) to "🧠" // 보라색
        // 기존 감정 타입들
        EmotionType.CALM -> Color(0xFFA5D6A7) to "😌" // 연한 초록색
        EmotionType.CONTENT -> Color(0xFF90CAF9) to "😄" // 연한 파란색
        EmotionType.ANXIOUS -> Color(0xFFF48FB1) to "😰" // 핑크색
        EmotionType.ENERGETIC -> Color(0xFFFFE082) to "⚡" // 노란색
        EmotionType.RELAXED -> Color(0xFFA5D6A7) to "😊" // 연한 초록색
        EmotionType.PROUD -> Color(0xFF90CAF9) to "😎" // 연한 파란색
        null -> Color.White to "-"
    }
}

/**
 * 숫자를 천 단위 구분자로 포맷팅
 */
private fun formatNumber(number: Int): String {
    return number.toString().reversed().chunked(3).joinToString(",").reversed()
}

/**
 * 기분 텍스트에 따른 이모지 반환
 */
private fun getMoodEmoji(mood: String): String {
    return when (mood.lowercase()) {
        "happy" -> "😊"
        "excited" -> "🤩"
        "calm" -> "😌"
        "content" -> "😄"
        "tired" -> "😴"
        "sad" -> "😢"
        "anxious" -> "😰"
        "energetic" -> "⚡"
        "relaxed" -> "😊"
        "proud" -> "😎"
        else -> "😊"
    }
}

/**
 * 월간 통계 데이터 클래스
 */
private data class MonthlyStats(
    val primaryMood: String,
    val description: String,
    val totalSteps: Int,
    val sessionsCount: Int,
    val focusScore: Int,
)

/**
 * 월간 통계 계산
 */
private fun calculateMonthlyStats(
    sessions: List<WalkingSession>,
    month: YearMonth,
    emotionsByDate: Map<LocalDate, List<Emotion>>,
): MonthlyStats {
    val monthStart = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val monthEnd = month.atEndOfMonth().atTime(23, 59, 59)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // 해당 월의 세션 필터링
    val monthSessions = sessions.filter { session ->
        session.startTime >= monthStart && session.startTime <= monthEnd
    }

    // 총 걸음 수
    val totalSteps = monthSessions.sumOf { it.stepCount.toLong() }.toInt()

    // 세션 수
    val sessionsCount = monthSessions.size

    // 감정별 카운트
    val emotionCounts = emotionsByDate.values.flatten()
        .filter { emotion ->
            val date = java.time.Instant.ofEpochMilli(emotion.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            date.year == month.year && date.monthValue == month.monthValue
        }
        .groupBy { it.type }
        .mapValues { it.value.size }

    // 가장 많은 감정 찾기
    val primaryEmotionType = emotionCounts.maxByOrNull { it.value }?.key ?: EmotionType.HAPPY
    val primaryMood = when (primaryEmotionType) {
        EmotionType.HAPPY -> "Happy"
        EmotionType.JOYFUL -> "Joyful"
        EmotionType.LIGHT_FOOTED -> "Light-footed"
        EmotionType.EXCITED -> "Excited"
        EmotionType.THRILLED -> "Thrilled"
        EmotionType.TIRED -> "Tired"
        EmotionType.SAD -> "Sad"
        EmotionType.DEPRESSED -> "Depressed"
        EmotionType.SLUGGISH -> "Sluggish"
        EmotionType.MANY_THOUGHTS -> "Many Thoughts"
        EmotionType.COMPLEX_MIND -> "Complex Mind"
        EmotionType.CALM -> "Calm"
        EmotionType.CONTENT -> "Content"
        EmotionType.ANXIOUS -> "Anxious"
        EmotionType.ENERGETIC -> "Energetic"
        EmotionType.RELAXED -> "Relaxed"
        EmotionType.PROUD -> "Proud"
    }

    val description = when (primaryEmotionType) {
        EmotionType.HAPPY -> "You're feeling happy and optimistic. Keep up the good vibes!"
        EmotionType.JOYFUL -> "You're feeling joyful and content. Enjoy this moment!"
        EmotionType.LIGHT_FOOTED -> "You're feeling light and energetic. Perfect for a walk!"
        EmotionType.EXCITED -> "You're full of energy and excitement. Channel it positively!"
        EmotionType.THRILLED -> "You're thrilled and excited. Make the most of this energy!"
        EmotionType.TIRED -> "You might need some rest. Take care of yourself!"
        EmotionType.SAD -> "It's okay to feel down sometimes. Remember, this too shall pass."
        EmotionType.DEPRESSED -> "Take it easy. Walking can help clear your mind."
        EmotionType.SLUGGISH -> "You're feeling sluggish. A walk might help energize you."
        EmotionType.MANY_THOUGHTS -> "Your mind is busy. Walking can help organize your thoughts."
        EmotionType.COMPLEX_MIND -> "Your mind is complex. Take a walk to clear your head."
        EmotionType.CALM -> "You're feeling peaceful and balanced. Maintain this tranquility!"
        EmotionType.CONTENT -> "You're satisfied and at ease. Enjoy this contentment!"
        EmotionType.ANXIOUS -> "Take deep breaths. You're stronger than you think."
        EmotionType.ENERGETIC -> "You're bursting with energy! Use it wisely."
        EmotionType.RELAXED -> "You're in a relaxed state. Enjoy this moment of peace."
        EmotionType.PROUD -> "You should be proud of yourself. Keep going!"
    }

    // Focus score 계산 (세션 완료율 기반, 30일 기준)
    val focusScore = if (sessionsCount > 0) {
        ((sessionsCount.toFloat() / 30f) * 100f).toInt().coerceIn(0, 100)
    } else {
        0
    }

    return MonthlyStats(
        primaryMood = primaryMood,
        description = description,
        totalSteps = totalSteps,
        sessionsCount = sessionsCount,
        focusScore = focusScore,
    )
}

