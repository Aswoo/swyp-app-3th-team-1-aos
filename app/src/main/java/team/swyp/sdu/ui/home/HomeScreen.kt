package team.swyp.sdu.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import team.swyp.sdu.presentation.viewmodel.NotificationPermissionViewModel
import team.swyp.sdu.ui.notification.NotificationPermissionDialog
import team.swyp.sdu.ui.home.components.WeeklyRecordCard
import team.swyp.sdu.ui.home.components.HomeHeader
import team.swyp.sdu.ui.home.components.DominantEmotionCard
import team.swyp.sdu.ui.home.components.EmotionIcon
import team.swyp.sdu.ui.home.components.HomeInfoCard
import team.swyp.sdu.ui.mission.component.MissionCard
import team.swyp.sdu.ui.mission.model.toCardState

/**
 * 홈 화면 (요약/캐릭터/미션/주간 기록)
 */
@Composable
fun HomeScreen(
    onClickWalk: () -> Unit = {},
    onClickAlarm: () -> Unit = {},
    onClickMission: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    permissionViewModel: NotificationPermissionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 알림 권한 요청 Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        permissionViewModel.handlePermissionResult(isGranted)
    }

    // 위치 권한 요청 Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 수락 → 데이터 재로드
            viewModel.reloadAfterPermissionGranted()
        }
        // 권한 거부 시 기본 위치로 이미 로드된 데이터 유지
    }

    // 홈 화면 진입 시 알림 권한 다이얼로그 표시 여부 확인
    LaunchedEffect(Unit) {
        permissionViewModel.checkShouldShowDialog()
        // 초기 데이터 로드는 ViewModel의 init에서 자동으로 수행됨
        // (기본 위치로 로드)
    }
    val missions = when (uiState) {
        is HomeUiState.Success -> (uiState as HomeUiState.Success).missions
        else -> emptyList()
    }

    val profileImageUrl = when (uiState) {
        is HomeUiState.Success -> {
            // TODO: 사용자 프로필 이미지 URL 가져오기
            ""
        }

        else -> ""
    }

    Column {
        HomeHeader(profileImageUrl = profileImageUrl, onClickAlarm = onClickAlarm)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
        ) {

            HomeInfoCard(homeUiState = uiState)

            Text(
                text = "오늘의 추천 미션",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                missions.forEach { mission ->
                    val cardState = mission.toCardState(isActive = true)
                    MissionCard(
                        mission = mission,
                        cardState = cardState,
                        onChallengeClick = onClickMission,
                        onRewardClick = { missionId ->
                            // TODO: 리워드 받기 로직 구현
                            onClickMission()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Text(
                text = "나의 산책 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            when (uiState) {
                is HomeUiState.Success -> {
                    val records = (uiState as HomeUiState.Success).sessionsThisWeek
                    Column {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            records.forEach { session ->
                                WeeklyRecordCard(
                                    session = session,
                                    modifier = Modifier.width(260.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // 이번주 주요 감정 카드
                        val dominantEmotion = (uiState as HomeUiState.Success).dominantEmotion
                        DominantEmotionCard(
                            emotionType = dominantEmotion,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val recentEmotions = (uiState as HomeUiState.Success).recentEmotions
                            val itemCount = recentEmotions.size.coerceAtMost(7)

                            repeat(7) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    if (index < itemCount) {
                                        recentEmotions[index]?.let { emotion ->
                                            EmotionIcon(emotionType = emotion)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                    }

                }

                is HomeUiState.Error -> {
                    Text(
                        text = (uiState as HomeUiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                HomeUiState.Loading -> {
                    Text(
                        text = "불러오는 중...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 나의 감정 기록 섹션
//        when (uiState) {
//            is HomeUiState.Success -> {
//                (uiState as HomeUiState.Success).weeklyEmotionSummary?.let { summary ->
//                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                        Text(
//                            text = "나의 감정 기록",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                        )
////                        EmotionRecordCard(emotionSummary = summary)
//                    }
//                }
//            }
//            else -> {}
//        }
        }
    }

    // 알림 권한 다이얼로그 표시
    NotificationPermissionDialog(
        viewModel = permissionViewModel,
        onRequestPermission = {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        },
    )
}
