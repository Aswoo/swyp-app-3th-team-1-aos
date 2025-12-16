package team.swyp.sdu.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.util.copy
import team.swyp.sdu.ui.components.AppHeader
import team.swyp.sdu.ui.components.CtaButton
import team.swyp.sdu.ui.components.MenuItem
import team.swyp.sdu.ui.components.SectionCard
import team.swyp.sdu.ui.theme.Grey10
import team.swyp.sdu.ui.theme.Grey3
import team.swyp.sdu.ui.theme.Grey7
import team.swyp.sdu.ui.theme.WalkItTheme
import team.swyp.sdu.ui.theme.walkItTypography

/**
 * 마이 페이지 화면
 *
 * @param modifier Modifier
 * @param onNavigateBack 뒤로가기 클릭 핸들러
 */
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    onNavigateCharacterEdit: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppHeader(
            title = "마이 페이지",
            onNavigateBack = onNavigateBack,
        )
        Row {
            Text(
                text = "닉네임",
                style = MaterialTheme.walkItTypography.headingM,
                color = Grey10
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "님",
                style = MaterialTheme.walkItTypography.headingM,
                color = Grey7
            )
        }

        CtaButton(
            text = "캐릭터 정보 수정",
            onClick = onNavigateCharacterEdit,
            modifier = Modifier.padding(horizontal = 50.dp)
        )

        Spacer(Modifier.height(32.dp))

        HorizontalDivider(thickness = 10.dp, color = Grey3)

        Spacer(Modifier.height(32.dp))

        SectionCard {
            Text(
                text = "설정",
                // body L/semibold
                style = MaterialTheme.walkItTypography.bodyL.copy(
                    fontWeight = FontWeight.SemiBold
                ), color = Grey10
            )
            Spacer(Modifier.height(8.dp))

            MenuItem("알람 설정", { })
            Spacer(Modifier.height(8.dp))
            MenuItem("내 정보 관리", {})
        }

        Spacer(Modifier.height(8.dp))

        SectionCard(
            modifier = Modifier
        ) {
            Text(
                text = "설정",
                // body L/semibold
                style = MaterialTheme.walkItTypography.bodyL.copy(
                    fontWeight = FontWeight.SemiBold
                ), color = Grey10
            )
            Spacer(Modifier.height(8.dp))

            MenuItem("내 목표 관리", { })
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "로그아웃",
                style = MaterialTheme.walkItTypography.bodyS,
                color = Grey7
            )

            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .height(16.dp),
                thickness = 1.dp,
                color = Grey3
            )

            Text(
                text = "탈퇴 하기",
                style = MaterialTheme.walkItTypography.bodyS,
                color = Grey7
            )
        }

    }
}


@Preview
@Composable
fun MyPagePreview() {
    WalkItTheme {
        MyPageScreen()
    }
}