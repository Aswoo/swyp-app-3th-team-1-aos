package team.swyp.sdu.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import team.swyp.sdu.ui.components.WheelPicker
import team.swyp.sdu.ui.theme.WalkItTheme

/**
 * 목표 설정 단계 컴포넌트
 */
@Composable
fun GoalStep(
    unit: String,
    unitMenuExpanded: Boolean,
    onUnitClick: () -> Unit,
    onUnitDismiss: () -> Unit,
    onUnitSelect: (String) -> Unit,
    goal: Int,
    onGoalChange: (Int) -> Unit,
    steps: Int,
    onStepsChange: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val periodNumber = 1
    val canProceed = goal > 0 && steps > 0

    // 바텀시트 표시 상태
    var showGoalPicker by remember { mutableStateOf(false) }
    var showStepsPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 숫자/단위 및 목표 클릭 영역 (드롭다운 + 휠)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$periodNumber",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Box {
                    ChipButton(
                        text = unit,
                        onClick = onUnitClick,
                    )
                    DropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = onUnitDismiss,
                    ) {
                        listOf("달", "주").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { onUnitSelect(option) },
                            )
                        }
                    }
                }
                Text(text = "/", style = MaterialTheme.typography.headlineSmall)
                ChipButton(
                    text = "${goal} 회",
                    onClick = { showGoalPicker = true },
                )
            }

            // 달/주 당 목표 걸음수
            ClickField(
                label = "${unit}에 몇 보",
                value = if (steps > 0) "$steps 보" else "선택",
                onClick = { showStepsPicker = true },
            )
            Text(text = "목표 확인 후 다음을 눌러주세요", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onPrev,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text("이전", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("다음", color = Color.White)
                }
            }
        }

        // 목표 횟수 선택 바텀시트
        if (showGoalPicker) {
            WheelPickerOverlay(
                visible = true,
                items = (1..100).map { it.toString() },
                initialIndex = (goal - 1).coerceIn(0, 99),
                title = "목표 횟수 선택",
                onConfirm = { idx, _ ->
                    onGoalChange(idx + 1)
                    showGoalPicker = false
                },
                onDismiss = { showGoalPicker = false },
            )
        }

        // 걸음수 선택 바텀시트
        if (showStepsPicker) {
            WheelPickerOverlay(
                visible = true,
                items = (1..20).map { (it * 1000).toString() },
                initialIndex = 0,
                title = "${unit}에 몇 보 설정",
                onConfirm = { _, value ->
                    onStepsChange(value.toInt())
                    showStepsPicker = false
                },
                onDismiss = { showStepsPicker = false },
            )
        }
    }
}

@Composable
private fun WheelPickerOverlay(
    visible: Boolean,
    items: List<String>,
    initialIndex: Int,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
) {
    if (!visible) return

    var index by remember { mutableStateOf(initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onDismiss) { Text("취소") }
                            TextButton(onClick = { onConfirm(index, items[index]) }) { Text("확인") }
                        }
                    }
                    WheelPicker(
                        items = items,
                        initialIndex = index,
                        onSelected = { i, _ -> index = i },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipButton(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF6F6F6),
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ClickField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick() },
            color = Color(0xFFF6F6F6),
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalStepPreview() {
    WalkItTheme {
        GoalStep(
            unit = "달",
            unitMenuExpanded = false,
            onUnitClick = {},
            onUnitDismiss = {},
            onUnitSelect = {},
            goal = 10,
            onGoalChange = {},
            steps = 0,
            onStepsChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalStepFilledPreview() {
    WalkItTheme {
        GoalStep(
            unit = "주",
            unitMenuExpanded = false,
            onUnitClick = {},
            onUnitDismiss = {},
            onUnitSelect = {},
            goal = 5,
            onGoalChange = {},
            steps = 10000,
            onStepsChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

