package team.swyp.sdu.ui.mission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import team.swyp.sdu.domain.model.MissionCategory

/**
 * 카테고리 칩 컴포넌트
 *
 * 필터링을 위한 카테고리 선택 칩
 */
@Composable
fun CategoryChip(
    category: MissionCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFE0E0E0) else Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
        )
    }
}
