package team.swyp.sdu.ui.friend.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import team.swyp.sdu.domain.model.FollowStatus
import team.swyp.sdu.ui.theme.Pretendard
import team.swyp.sdu.ui.theme.TypeScale

/**
 * 친구 카드 컴포넌트
 *
 * Figma 디자인 기반 친구 목록/검색 결과 카드
 * - 프로필 이미지 (36x36, 원형)
 * - 닉네임 텍스트 (body M/medium, 16px)
 * - 팔로우 버튼 (상태에 따라 변경)
 * - 하단 Divider
 *
 * @param nickname 닉네임
 * @param imageName 프로필 이미지 이름 (선택사항)
 * @param followStatus 친구 요청 상태
 * @param onFollowClick 팔로우 버튼 클릭 핸들러
 * @param modifier Modifier
 */
@Composable
fun FriendCard(
    nickname: String,
    imageName: String? = null,
    followStatus: FollowStatus,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF)) // color/background/whtie-primary
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 왼쪽: 프로필 이미지 + 닉네임
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 프로필 이미지 (36x36, 원형)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color(0xFFF3F3F5), // color/text-border/secondary-inverse
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // TODO: 이미지 로딩 구현 시 Coil 사용
                    // 현재는 placeholder로 빈 원형 배경만 표시
                }

                // 닉네임
                Text(
                    text = nickname,
                    fontFamily = Pretendard,
                    fontSize = TypeScale.BodyM, // 16sp
                    fontWeight = FontWeight.Medium, // Medium
                    lineHeight = (TypeScale.BodyM.value * 1.5f).sp, // lineHeight 1.5
                    letterSpacing = (-0.16f).sp, // letterSpacing -0.16px
                    color = Color(0xFF191919), // color/text-border/primary
                )
            }

            // 오른쪽: 팔로우 버튼
            FollowButton(
                followStatus = followStatus,
                onClick = onFollowClick,
            )
        }

        // 하단 Divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color(0xFFF3F3F5), // color/text-border/secondary-inverse
            thickness = 1.dp,
        )
    }
}

/**
 * 팔로우 버튼 컴포넌트
 *
 * 상태에 따라 버튼 텍스트와 스타일이 변경됩니다.
 */
@Composable
private fun FollowButton(
    followStatus: FollowStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (buttonText, buttonColor) = when (followStatus) {
        FollowStatus.PENDING -> "팔로우" to Color(0xFF52CE4B) // color/button/primary-default
        FollowStatus.FOLLOWING -> "팔로잉" to Color(0xFFE0E0E0) // 비활성화 색상
        FollowStatus.NONE -> "팔로우" to Color(0xFF52CE4B) // color/button/primary-default
    }

    val textColor = when (followStatus) {
        FollowStatus.FOLLOWING -> Color(0xFF9E9E9E) // 비활성화 텍스트 색상
        else -> Color(0xFFFFFFFF) // color/text-border/primary-inverse
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor,
        ),
        shape = RoundedCornerShape(8.dp), // 둥근 모서리
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = buttonText,
            fontFamily = Pretendard,
            fontSize = TypeScale.BodyM, // 16sp
            fontWeight = FontWeight.Medium, // Medium
            lineHeight = (TypeScale.BodyM.value * 1.5f).sp, // lineHeight 1.5
            letterSpacing = (-0.16f).sp, // letterSpacing -0.16px
            color = textColor,
        )
    }
}

