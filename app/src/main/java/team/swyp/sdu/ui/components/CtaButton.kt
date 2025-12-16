package team.swyp.sdu.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import team.swyp.sdu.ui.theme.Pretendard
import team.swyp.sdu.ui.theme.TypeScale
import team.swyp.sdu.ui.theme.walkItTypography

/**
 * CTA 버튼 컴포넌트
 *
 * Figma 디자인 기반 공용 버튼
 * - 기본 상태: 녹색 배경 (#52ce4b)
 * - 눌린 상태: 동일한 디자인 (Material3 기본 pressed 상태)
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier Modifier
 * @param enabled 버튼 활성화 여부 (기본값: true)
 * @param icon 아이콘 (선택사항, 향후 확장 가능)
 */
@Composable
fun CtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Figma 디자인 색상
    val buttonColor = Color(0xFF52CE4B) // color/button/primary-default
    val textColor = Color(0xFFFFFFFF) // color/text-border/primary-inverse
    val disabledColor = Color(0xFFE0E0E0) // 비활성화 색상 (임시)

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp), // Figma 디자인의 둥근 모서리
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) buttonColor else disabledColor,
            contentColor = textColor,
            disabledContainerColor = disabledColor,
            disabledContentColor = Color(0xFF9E9E9E),
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 16.dp,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPressed) 0.dp else 2.dp, // 눌린 상태에서 elevation 제거
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        // 아이콘이 있으면 왼쪽에 배치 (향후 확장 가능)
        icon?.invoke()

        Text(
            text = text,
            style = MaterialTheme.walkItTypography.bodyM,
            color = textColor,
        )
    }
}

/**
 * CtaButton Preview
 */
@Composable
fun CtaButtonPreview() {
    CtaButton(
        text = "CTA button",
        onClick = {},
    )
}

