package team.swyp.sdu.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.presentation.viewmodel.GoalManagementViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 내 목표 관리 화면
 * Figma 디자인 기반 구현
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalManagementViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val goalState by viewModel.goalState.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }
    var selectedSteps by remember { mutableIntStateOf(goalState.targetSteps) }
    var selectedStartDate by remember { mutableStateOf(goalState.startDate) }
    var selectedEndDate by remember { mutableStateOf(goalState.endDate) }
    var selectedFrequency by remember { mutableIntStateOf(goalState.walkFrequency) }
    var selectedMissionCount by remember { mutableIntStateOf(goalState.missionSuccessCount) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내 목표 관리",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { onNavigateBack() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                ),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
        ) {
            // 목표 설정 섹션
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    // 걸음 수
                    GoalItemRow(
                        label = "걸음 수",
                        value = "${selectedSteps.toInt().formatWithComma()}보",
                        isEditing = isEditing,
                        onEditClick = {
                            // TODO: 걸음 수 선택 다이얼로그
                        },
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // 기간
                    GoalItemRow(
                        label = "기간",
                        value = "${formatDate(selectedStartDate)} ~ ${formatDate(selectedEndDate)}",
                        isEditing = isEditing,
                        onEditClick = {
                            // TODO: 날짜 선택 다이얼로그
                        },
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // 산책 빈도
                    GoalItemRow(
                        label = "산책 빈도",
                        value = "${selectedFrequency}회",
                        isEditing = isEditing,
                        onEditClick = {
                            // TODO: 산책 빈도 선택 다이얼로그
                        },
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // 미션 성공 횟수
                    GoalItemRow(
                        label = "미션 성공 횟수",
                        value = "${selectedMissionCount}회",
                        isEditing = isEditing,
                        onEditClick = {
                            // TODO: 미션 성공 횟수 선택 다이얼로그
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 액션 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                            viewModel.updateGoal(
                                targetSteps = selectedSteps,
                                startDate = selectedStartDate,
                                endDate = selectedEndDate,
                                walkFrequency = selectedFrequency,
                                missionSuccessCount = selectedMissionCount,
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

            // 초기화 링크
            TextButton(
                onClick = {
                    viewModel.resetGoal()
                    selectedSteps = 10000
                    selectedStartDate = Calendar.getInstance().timeInMillis
                    selectedEndDate = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 7)
                    }.timeInMillis
                    selectedFrequency = 3
                    selectedMissionCount = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "초기화",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}

@Composable
private fun GoalItemRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable(enabled = isEditing, onClick = onEditClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
            )
            if (isEditing) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF666666),
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.getDefault(), "%02d월 %02d일", month, day)
}

private fun Int.formatWithComma(): String {
    return String.format(Locale.getDefault(), "%,d", this)
}
