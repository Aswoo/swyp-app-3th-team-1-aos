package team.swyp.sdu.ui.mypage.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.domain.goal.GoalRange
import team.swyp.sdu.ui.components.AppHeader
import team.swyp.sdu.ui.components.InfoBanner
import team.swyp.sdu.ui.mypage.goal.component.GoalSettingCard
import team.swyp.sdu.ui.theme.WalkItTheme
import team.swyp.sdu.ui.theme.White

/**
 * 내 목표 관리 Route
 *
 * ViewModel을 주입받고 상태를 수집하여 Screen에 전달합니다.
 */
@Composable
fun GoalManagementRoute(
    viewModel: GoalManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GoalManagementScreen(
        goalState = uiState,
        onNavigateBack = onNavigateBack,
        onUpdateGoal = viewModel::updateGoal,
        onResetGoal = viewModel::resetGoal,
    )
}

/**
 * 내 목표 관리 화면
 * Figma 디자인 기반 구현
 */
@Composable
fun GoalManagementScreen(
    goalState: GoalState,
    onNavigateBack: () -> Unit,
    onUpdateGoal: (
        targetSteps: Int,
        startDate: Long,
        endDate: Long,
        walkFrequency: Int,
        missionSuccessCount: Int,
    ) -> Unit,
    onResetGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {

    var isEditing by remember { mutableStateOf(false) }
    var selectedSteps by remember { mutableIntStateOf(goalState.targetSteps) }
    var selectedStartDate by remember { mutableStateOf(goalState.startDate) }
    var selectedEndDate by remember { mutableStateOf(goalState.endDate) }
    var selectedFrequency by remember { mutableIntStateOf(goalState.walkFrequency) }
    var selectedMissionCount by remember { mutableIntStateOf(goalState.missionSuccessCount) }

    // 서버 데이터가 로드되면 로컬 상태 업데이트
    LaunchedEffect(goalState) {
        selectedSteps = goalState.targetSteps
        selectedFrequency = goalState.walkFrequency
        selectedMissionCount = goalState.missionSuccessCount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        // 헤더
        AppHeader(
            title = "내 목표 관리",
            onNavigateBack = onNavigateBack,
        )

        // 메인 콘텐츠
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 목표 설정 섹션

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Spacer(Modifier.height(16.dp))
                InfoBanner(
                    title = "목표는 설정일부터 1주일 기준으로 설정 가능합니다.",
                    description = "목표는 1주 내 최소 1회, 최대 7회까지 설정 가능합니다"
                )
                Spacer(Modifier.height(20.dp))
                GoalSettingCard(
                    title = "주간 산책 횟수",
                    currentNumber = selectedFrequency,
                    onClickPlus = {
                        if (selectedFrequency < 7) selectedFrequency++
                    },
                    onClickMinus = {
                        if (selectedFrequency > 1) selectedFrequency--
                    },
                    onNumberChange = { selectedFrequency = it },
                    range = GoalRange(1, 7),
                    unit = "회"
                )

                Spacer(Modifier.height(24.dp))

                GoalSettingCard(
                    title = "목표 걸음 수",
                    currentNumber = selectedSteps,
                    onClickPlus = {
                        if (selectedSteps < 100000) selectedSteps += 1000
                    },
                    onClickMinus = {
                        if (selectedSteps > 1000) selectedSteps -= 1000
                    },
                    onNumberChange = { selectedSteps = it },
                    range = GoalRange(1000, 100000),
                    unit = "보"
                )
                Spacer(modifier = Modifier.weight(1f))
                // 액션 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            if (isEditing) {
                                // 편집 취소
                                selectedSteps = goalState.targetSteps
                                selectedStartDate = goalState.startDate
                                selectedEndDate = goalState.endDate
                                selectedFrequency = goalState.walkFrequency
                                selectedMissionCount = goalState.missionSuccessCount
                            }
                            isEditing = !isEditing
                            if (!isEditing) {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = if (isEditing) "취소" else "뒤로가기",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    Button(
                        onClick = {
                            if (isEditing) {
                                // 저장
                                onUpdateGoal(
                                    selectedSteps,
                                    selectedStartDate,
                                    selectedEndDate,
                                    selectedFrequency,
                                    selectedMissionCount,
                                )
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = if (isEditing) "저장" else "편집하기",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun GoalManagementScreenPreview() {
    WalkItTheme {
        GoalManagementScreen(
            goalState = GoalState(),
            onNavigateBack = {},
            onUpdateGoal = { _, _, _, _, _ -> },
            onResetGoal = {})
    }
}