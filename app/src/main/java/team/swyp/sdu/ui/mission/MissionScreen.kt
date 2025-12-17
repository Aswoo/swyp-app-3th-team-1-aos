package team.swyp.sdu.ui.mission

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.domain.model.MissionCategory
import team.swyp.sdu.presentation.viewmodel.MissionUiState
import team.swyp.sdu.presentation.viewmodel.MissionViewModel
import team.swyp.sdu.ui.components.AppHeader
import team.swyp.sdu.ui.mission.component.CategoryChip
import team.swyp.sdu.ui.mission.component.MissionCard
import team.swyp.sdu.ui.mission.component.PopularMissionCard
import team.swyp.sdu.ui.theme.WalkItTheme


@Composable
fun MissionRoute(
    onNavigateBack: () -> Unit,
    onNavigateToMissionDetail: (String) -> Unit = {},
) {
    val viewModel: MissionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MissionScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onMissionClick = { missionId ->
            onNavigateToMissionDetail(missionId)
        },
    )
}

@Composable
fun MissionScreen(
    uiState: MissionUiState,
    onNavigateBack: () -> Unit,
    onMissionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<MissionCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 헤더
        AppHeader(
            title = "미션",
            onNavigateBack = onNavigateBack,
        )

        // 메인 콘텐츠
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 이번 주 인기 미션 섹션
            item {
                PopularMissionCard()
            }

            // 카테고리별 미션 섹션
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "카테고리별 미션",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE0E0E0))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { /* TODO: 필터 다이얼로그 */ },
                        ) {
                            Text(
                                text = "필터",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                            )
                        }
                    }

                    // 필터 칩
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MissionCategory.entries.forEach { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = if (selectedCategory == category) null else category },
                            )
                        }
                    }
                }
            }

            // 미션 리스트
            items(uiState.weeklyMissions) { mission ->
                MissionCard(
                    mission = mission,
                    onClick = { onMissionClick(mission.userWeeklyMissionId.toString()) },
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
@Preview
fun MissionPreview(){
    WalkItTheme {
        MissionScreen(
            uiState = MissionUiState(),
            onNavigateBack = {},
            onMissionClick = {},
        )
    }
}