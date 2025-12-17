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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import team.swyp.sdu.domain.model.Sex
import team.swyp.sdu.ui.components.WheelPicker
import team.swyp.sdu.ui.theme.WalkItTheme

/**
 * 출생년월일 선택 단계 컴포넌트
 */
@Composable
fun BirthYearStep(
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    currentSex: Sex?,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onSexChange: (Sex) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val canProceed = try {
        val yearValid = currentYear in 1901..LocalDate.now().year
        val monthValid = currentMonth in 1..12
        val dayValid = try {
            LocalDate.of(currentYear, currentMonth, currentDay)
            true
        } catch (e: Exception) {
            false
        }
        val sexValid = currentSex != null
        yearValid && monthValid && dayValid && sexValid
    } catch (e: Exception) {
        false
    }

    // 해당 월의 마지막 날짜 계산
    val daysInMonth = remember(currentYear, currentMonth) {
        try {
            LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth()
        } catch (e: Exception) {
            31
        }
    }

    // 일자가 유효 범위를 벗어나면 자동으로 조정
    LaunchedEffect(currentYear, currentMonth, currentDay, daysInMonth) {
        val safeDay = currentDay.coerceIn(1, daysInMonth)
        if (safeDay != currentDay) {
            onDayChange(safeDay)
        }
    }

    // 바텀시트 표시 상태
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showSexPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "출생 정보와 성별을 선택하세요",
                style = MaterialTheme.typography.headlineSmall,
            )

            // 출생년월일 선택 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 년도 선택 필드
                ClickField(
                    label = "년",
                    value = "$currentYear",
                    onClick = { showYearPicker = true },
                    modifier = Modifier.weight(1f),
                )

                // 월 선택 필드
                ClickField(
                    label = "월",
                    value = "$currentMonth",
                    onClick = { showMonthPicker = true },
                    modifier = Modifier.weight(1f),
                )

                // 일 선택 필드
                ClickField(
                    label = "일",
                    value = "$currentDay",
                    onClick = { showDayPicker = true },
                    modifier = Modifier.weight(1f),
                )
            }

            // 성별 선택 필드
            ClickField(
                label = "성별",
                value = when (currentSex) {
                    Sex.MALE -> "남성"
                    Sex.FEMALE -> "여성"
                    null -> "선택하세요"
                },
                onClick = { showSexPicker = true },
                modifier = Modifier.fillMaxWidth(),
            )

            // 만 나이 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "만",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF6F6F6),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val age = LocalDate.now().year - currentYear
                        Text(
                            text = "$age 세",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

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
                    Text("완료", color = Color.White)
                }
            }
        }

        // 년도 선택 바텀시트
        if (showYearPicker) {
            WheelPickerOverlay(
                visible = true,
                items = (1950..LocalDate.now().year).map { "$it" },
                initialIndex = (currentYear - 1950).coerceIn(0, LocalDate.now().year - 1950),
                title = "출생년도 선택",
                onConfirm = { _, value ->
                    onYearChange(value.toInt())
                    showYearPicker = false
                },
                onDismiss = { showYearPicker = false },
            )
        }

        // 월 선택 바텀시트
        if (showMonthPicker) {
            WheelPickerOverlay(
                visible = true,
                items = (1..12).map { "$it" },
                initialIndex = (currentMonth - 1).coerceIn(0, 11),
                title = "출생월 선택",
                onConfirm = { _, value ->
                    val month = value.toInt()
                    onMonthChange(month)
                    // 월 변경 시 일자 범위 조정
                    val newDaysInMonth = try {
                        LocalDate.of(currentYear, month, 1).lengthOfMonth()
                    } catch (e: Exception) {
                        31
                    }
                    if (currentDay > newDaysInMonth) {
                        onDayChange(newDaysInMonth)
                    }
                    showMonthPicker = false
                },
                onDismiss = { showMonthPicker = false },
            )
        }

        // 일 선택 바텀시트
        if (showDayPicker) {
            WheelPickerOverlay(
                visible = true,
                items = (1..daysInMonth).map { "$it" },
                initialIndex = (currentDay.coerceIn(1, daysInMonth) - 1).coerceIn(0, daysInMonth - 1),
                title = "출생일 선택",
                onConfirm = { _, value ->
                    onDayChange(value.toInt())
                    showDayPicker = false
                },
                onDismiss = { showDayPicker = false },
            )
        }

        // 성별 선택 바텀시트
        if (showSexPicker) {
            WheelPickerOverlay(
                visible = true,
                items = listOf("남성", "여성"),
                initialIndex = when (currentSex) {
                    Sex.MALE -> 0
                    Sex.FEMALE -> 1
                    null -> 0
                },
                title = "성별 선택",
                onConfirm = { _, value ->
                    val sex = when (value) {
                        "남성" -> Sex.MALE
                        "여성" -> Sex.FEMALE
                        else -> Sex.MALE
                    }
                    onSexChange(sex)
                    showSexPicker = false
                },
                onDismiss = { showSexPicker = false },
            )
        }
    }
}

@Composable
private fun ClickField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        Surface(
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
private fun BirthYearStepPreview() {
    WalkItTheme {
        BirthYearStep(
            currentYear = 1998,
            currentMonth = 5,
            currentDay = 15,
            currentSex = null,
            onYearChange = {},
            onMonthChange = {},
            onDayChange = {},
            onSexChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BirthYearStepYoungPreview() {
    WalkItTheme {
        BirthYearStep(
            currentYear = 2010,
            currentMonth = 12,
            currentDay = 25,
            currentSex = Sex.MALE,
            onYearChange = {},
            onMonthChange = {},
            onDayChange = {},
            onSexChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

