package team.swyp.sdu.ui.screens
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.concurrent.TimeUnit
import team.swyp.sdu.data.model.LocationPoint
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.presentation.viewmodel.KakaoMapViewModel
import team.swyp.sdu.presentation.viewmodel.WalkingSessionListUiState
import team.swyp.sdu.presentation.viewmodel.WalkingSessionListViewModel
import team.swyp.sdu.presentation.viewmodel.WalkingViewModel
import team.swyp.sdu.ui.components.KakaoMapView

/**
 * 산책 결과 화면
 *
 * 산책 완료 후 결과를 표시하는 별도 화면입니다.
 * 지도에 경로를 표시합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkingResultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRouteDetail: (List<LocationPoint>) -> Unit = {},
    viewModel: WalkingViewModel = hiltViewModel(),
    mapViewModel: KakaoMapViewModel = hiltViewModel(),
    sessionListViewModel: WalkingSessionListViewModel = hiltViewModel(),
) {
    // ViewModel에서 세션 가져오기
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Location 리스트 구독 (Shared ViewModel 구조)
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    
    // 스냅샷 상태 구독
    val snapshotState by mapViewModel.snapshotState.collectAsStateWithLifecycle()

    // Completed 상태가 아니면 에러 처리
    val session =
        when (val state = uiState) {
            is team.swyp.sdu.presentation.viewmodel.WalkingUiState.Completed -> {
                state.session
            }

            else -> {
                // Completed 상태가 아니면 기본 세션 반환 (또는 에러 처리)
                WalkingSession(startTime = System.currentTimeMillis())
            }
        }
    
    // Location 리스트를 KakaoMapViewModel에 전달 (Shared ViewModel 구조)
    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
            mapViewModel.setLocations(locations)
        }
    }

    // 이번 주 세션 상태 계산
    val sessionListState by sessionListViewModel.uiState.collectAsStateWithLifecycle()
    val weekCompletion = remember(sessionListState) {
        val sessions =
            when (sessionListState) {
                is WalkingSessionListUiState.Success -> (sessionListState as WalkingSessionListUiState.Success).sessions
                else -> emptyList()
            }

        val today = LocalDate.now()
        val startOfWeek = today.with(ChronoField.DAY_OF_WEEK, 1) // 월요일 시작
        val endOfWeek = startOfWeek.plusDays(6)

        val defaultMap = DayOfWeek.values().associateWith { false }.toMutableMap()
        sessions.forEach { s ->
            val date =
                Instant.ofEpochMilli(s.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            if (!date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)) {
                defaultMap[date.dayOfWeek] = true
            }
        }
        defaultMap.toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("산책 완료") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 지도 뷰에 경로 표시
            // 지도는 스크롤 가능한 영역 밖에 배치하여 터치 이벤트가 제대로 전달되도록 함
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                // KakaoMapView로 경로 표시
                // locations는 LaunchedEffect에서 mapViewModel에 이미 전달됨
                KakaoMapView(
                    locations = locations.ifEmpty { session.locations },
                    modifier = Modifier.fillMaxSize(),
                    viewModel = mapViewModel,
                )
            }

            // 나머지 콘텐츠는 스크롤 가능하도록 별도 Column에 배치
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 다시 시작하기 버튼
                Button(
                    onClick = {
                        // ViewModel 초기화 후 Main 화면으로 이동
                        // Main 화면의 WalkingScreen에서 LaunchedEffect가 Completed 상태를 감지하고
                        // 이미 초기화되어 있으므로 추가 초기화는 불필요하지만, 명시적으로 호출
                        viewModel.reset()
                        mapViewModel.reset() // 지도 ViewModel도 초기화
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("다시 시작하기")
                }

                // 스냅샷 이미지 표시
                snapshotState?.let { bitmap ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "지도 스냅샷",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                // 요약 카드 (하단 배치)
                WalkingSummaryCard(
                    durationMillis = session.duration,
                    steps = session.stepCount,
                    distanceMeters = session.totalDistance,
                )

                // 주간 달성 현황 (하단 배치)
                WeeklyCompletionCard(weekCompletion = weekCompletion)
            }
        }
    }
}

/**
 * 산책 요약 카드
 */
@Composable
private fun WalkingSummaryCard(
    durationMillis: Long,
    steps: Int,
    distanceMeters: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryItem(
                title = "산책 시간",
                value = formatHourMinute(durationMillis),
                suffix = "",
            )
            DividerDot()
            SummaryItem(
                title = "걸음 수",
                value = "%,d".format(steps),
                suffix = "",
            )
            DividerDot()
            SummaryItem(
                title = "총 거리",
                value = formatDistanceKm(distanceMeters),
                suffix = "km",
            )
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    value: String,
    suffix: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 28.sp,
        )
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DividerDot() {
    Box(
        modifier =
            Modifier
                .size(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
    )
}

/**
 * 주간 달성 현황 카드
 */
@Composable
private fun WeeklyCompletionCard(
    weekCompletion: Map<DayOfWeek, Boolean>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "이번주 목표 달성 현황",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DayOfWeek.values().forEach { day ->
                    WeeklyDayChip(
                        dayLabel = dayLabel(day),
                        isCompleted = weekCompletion[day] == true,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyDayChip(
    dayLabel: String,
    isCompleted: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .background(
                        color =
                            if (isCompleted) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.White
                            },
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun dayLabel(day: DayOfWeek): String =
    when (day) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

private fun formatHourMinute(millis: Long): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return String.format("%02d시간 %02d분", hours, minutes)
}

private fun formatDistanceKm(meters: Float): String {
    val km = meters / 1000f
    return String.format("%02.0f", km)
}
