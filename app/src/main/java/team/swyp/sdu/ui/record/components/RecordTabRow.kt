package team.swyp.sdu.ui.record.components

import android.view.RoundedCorner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import team.swyp.sdu.ui.theme.SemanticColor
import team.swyp.sdu.ui.theme.walkItTypography

/**
 * 기록 탭 타입
 */
enum class RecordTabType {
    Month,
    Week,
}

/**
 * 기록 화면 탭 행 컴포넌트
 */
@Composable
fun RecordTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = RecordTabType.entries

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .height(57.dp),
        divider = {},
        indicator = {},
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedTabIndex == index
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                modifier =
                    Modifier
                        .padding(10.dp)
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(
                            if (selected) SemanticColor.stateAquaBluePrimary else Color.Transparent,
                        ),
                text = {
                    Text(
                        text =
                            when (tab) {
                                RecordTabType.Month -> "월간"
                                RecordTabType.Week -> "주간"
                            },
                        color =
                            if (selected) SemanticColor.textBorderPrimaryInverse
                            else SemanticColor.textBorderSecondary,

                        style = MaterialTheme.walkItTypography.bodyS.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}



