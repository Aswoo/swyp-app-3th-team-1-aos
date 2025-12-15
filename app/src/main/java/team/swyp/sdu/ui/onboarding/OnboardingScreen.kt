package team.swyp.sdu.ui.onboarding

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import team.swyp.sdu.ui.components.WheelPicker
import team.swyp.sdu.ui.theme.WalkItTheme
import androidx.hilt.navigation.compose.hiltViewModel
import team.swyp.sdu.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    data class PickerOverlay(
        val title: String,
        val items: List<String>,
        val initialIndex: Int,
        val onConfirm: (Int, String) -> Unit,
    )

    var step by remember { mutableStateOf(0) }
    var nickname by remember { mutableStateOf("") }
    var goalCount by remember { mutableStateOf(10) }
    var birthYear by remember { mutableStateOf(LocalDate.now().year - 26) }
    var stepTarget by remember { mutableStateOf(0) }
    var unit by remember { mutableStateOf("달") }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var pickerOverlay by remember { mutableStateOf<PickerOverlay?>(null) }
    val scope = rememberCoroutineScope()

    val canNext by remember(step, nickname, goalCount, stepTarget, birthYear) {
        derivedStateOf {
            when (step) {
                0 -> nickname.trim().isNotEmpty()
                1 -> goalCount > 0 && stepTarget > 0
                2 -> birthYear in 1901..LocalDate.now().year
                else -> false
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .padding(20.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StepHeader(step = step)

            when (step) {
                0 -> NicknameStep(
                    value = nickname,
                    onChange = { nickname = it },
                )
                1 -> GoalStep(
                    unit = unit,
                unitMenuExpanded = showUnitDropdown,
                onUnitClick = { showUnitDropdown = true },
                onUnitDismiss = { showUnitDropdown = false },
                onUnitSelect = {
                    unit = it
                    showUnitDropdown = false
                },
                    goal = goalCount,
                    onGoalClick = {
                        val items = (1..100).map { it.toString() }
                        pickerOverlay =
                            PickerOverlay(
                                title = "목표 횟수 선택",
                                items = items,
                                initialIndex = (goalCount - 1).coerceIn(0, items.lastIndex),
                                onConfirm = { idx, _ -> goalCount = idx + 1 },
                            )
                    },
                    steps = stepTarget,
                    onStepsClick = {
                        val items = (1..20).map { (it * 1000).toString() }
                        pickerOverlay =
                            PickerOverlay(
                                title = "달에 몇 보 설정",
                                items = items,
                                initialIndex = 0,
                                onConfirm = { _, value -> stepTarget = value.toInt() },
                            )
                    },
                )
                2 -> BirthYearStep(
                    currentYear = birthYear,
                    onClick = {
                        val years = (1950..LocalDate.now().year).map { it.toString() }
                        pickerOverlay =
                            PickerOverlay(
                                title = "출생년도 선택",
                                items = years,
                                initialIndex = years.indexOf(birthYear.toString()).coerceAtLeast(0),
                                onConfirm = { _, value -> birthYear = value.toInt() },
                            )
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            RowButtons(
                step = step,
        onPrev = { if (step == 0) onNavigateBack() else step -= 1 },
        onNext = {
            if (step >= 2) {
                scope.launch {
                    viewModel.setCompleted()
                    onFinish()
                }
            } else {
                step += 1
            }
        },
        nextEnabled = canNext,
            )
        }
    }

    pickerOverlay?.let { overlay ->
        WheelPickerOverlay(
            visible = true,
            items = overlay.items,
            initialIndex = overlay.initialIndex,
            title = overlay.title,
            onConfirm = { idx, value ->
                overlay.onConfirm(idx, value)
                pickerOverlay = null
            },
            onDismiss = { pickerOverlay = null },
        )
    }
}

@Composable
private fun StepHeader(step: Int) {
    val badge = when (step) {
        0 -> "준비 단계"
        1 -> "목표 설정"
        else -> "정보 입력"
    }
    val title =
        when (step) {
            0 -> "캐릭터의 닉네임을 지어주세요"
            1 -> "워킷과 얼마나 걸어보시겠어요?"
            else -> "태어난 연도를 선택해주세요"
        }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NicknameStep(
    value: String,
    onChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("닉네임") },
            placeholder = { Text("(입력칸)") },
            singleLine = true,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            color = Color(0xFFE6E6E6),
            shape = RoundedCornerShape(24.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("캐릭터 이미지", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GoalStep(
    unit: String,
    unitMenuExpanded: Boolean,
    onUnitClick: () -> Unit,
    onUnitDismiss: () -> Unit,
    onUnitSelect: (String) -> Unit,
    goal: Int,
    onGoalClick: () -> Unit,
    steps: Int,
    onStepsClick: () -> Unit,
) {
    val periodNumber = 1
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                onClick = onGoalClick,
            )
        }

        // 달/주 당 목표 걸음수
        ClickField(
            label = "${unit}에 몇 보",
            value = if (steps > 0) "$steps 보" else "선택",
            onClick = onStepsClick,
        )
        Text(text = "목표 확인 후 다음을 눌러주세요", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BirthYearStep(
    currentYear: Int,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "태어난 연도를 선택하세요")
        ClickField(
            label = "출생년도",
            value = "$currentYear 년 (만 ${LocalDate.now().year - currentYear} 세)",
            onClick = onClick,
        )
    }
}

@Composable
private fun RowButtons(
    step: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onPrev,
            modifier = Modifier.weight(1f),
        ) { Text(if (step == 0) "취소" else "이전") }

        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = nextEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) { Text(if (step >= 2) "완료" else "다음", color = Color.White) }
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

@Composable
private fun WheelPickerOverlay(
    visible: Boolean,
    items: List<String>,
    initialIndex: Int,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    scrimDismiss: Boolean = false,
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
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = { if (scrimDismiss) onDismiss() },
                )
                .pointerInput(Unit) {
                    // 스크롤 제스처 등 모든 터치를 소비하여 뒤로 전달되지 않게 처리
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                },
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
                        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    WalkItTheme {
        OnboardingScreen()
    }
}


