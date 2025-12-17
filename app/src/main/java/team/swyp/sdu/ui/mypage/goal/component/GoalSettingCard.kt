package team.swyp.sdu.ui.mypage.goal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import team.swyp.sdu.R
import team.swyp.sdu.domain.goal.GoalRange
import team.swyp.sdu.ui.components.NumberFormField
import team.swyp.sdu.ui.theme.Green
import team.swyp.sdu.ui.theme.Grey10
import team.swyp.sdu.ui.theme.WalkItTheme
import team.swyp.sdu.ui.theme.White
import team.swyp.sdu.ui.theme.walkItTypography

@Composable
fun GoalSettingCard(
    title: String,
    modifier: Modifier = Modifier,
    currentNumber: Int,
    onNumberChange: (Int) -> Unit,
    range: GoalRange,
    unit: String,
    onClickMinus: () -> Unit = {},
    onClickPlus: () -> Unit = {},
) {
    val ControlHeight = 48.dp
    val ButtonShape = RoundedCornerShape(8.dp)

    Column(modifier = modifier) {

        Text(
            text = title,
            style = MaterialTheme.walkItTypography.bodyM,
            color = Grey10
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = "최소 ${range.min}${unit} ~ 최대 ${range.max}${unit}",
            style = MaterialTheme.walkItTypography.captionM,
            color = Grey10
        )

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 숫자 표시 (읽기 전용)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = ControlHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = currentNumber.toString(),
                    style = MaterialTheme.walkItTypography.bodyM,
                    color = Grey10,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // - 버튼
            Box(
                modifier = Modifier
                    .size(ControlHeight)
                    .clip(ButtonShape)
                    .background(White)
                    .border(
                        width = 1.dp,
                        color = Green,
                        shape = ButtonShape
                    )
                    .clickable(onClick = onClickMinus),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_action_minus),
                    contentDescription = "감소",
                    modifier = Modifier.size(20.dp),
                    tint = Green
                )
            }

            Spacer(Modifier.width(4.dp))

            // + 버튼
            Box(
                modifier = Modifier
                    .size(ControlHeight) // ⭐ 핵심: 높이 통일
                    .clip(ButtonShape)
                    .background(Green)
                    .clickable(onClick = onClickPlus),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_action_plus),
                    contentDescription = "증가",
                    modifier = Modifier.size(20.dp),
                    tint = White
                )
            }
        }
    }
}


@Composable
@Preview
fun GoalSettingCardPreview() {
    WalkItTheme {
        Surface(color = White) {
            GoalSettingCard(
                title = "title",
                modifier = Modifier,
                currentNumber = 1,
                onNumberChange = {},
                range = GoalRange(1, 10),
                unit = "회",
                onClickMinus = {},
                onClickPlus = {}
            )
        }
    }
}
