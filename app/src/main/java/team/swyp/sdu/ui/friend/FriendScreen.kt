package team.swyp.sdu.ui.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.domain.model.Friend
import team.swyp.sdu.ui.friend.FriendViewModel
import team.swyp.sdu.ui.theme.Pretendard
import team.swyp.sdu.ui.theme.TypeScale
import team.swyp.sdu.ui.theme.WalkItTheme

@Composable
fun FriendScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    var menuTargetId by remember { mutableStateOf<String?>(null) }
    var confirmTarget by remember { mutableStateOf<Friend?>(null) }

    // 검색어가 입력되면 검색 결과 화면으로 이동
    LaunchedEffect(query) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            // ViewModel에서 debounce 처리되므로 바로 이동
            onNavigateToSearch()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(onNavigateBack = onNavigateBack)

        SearchBar(
            query = query,
            onQueryChange = viewModel::updateQuery,
            onClear = {
                viewModel.clearQuery()
            },
        )

        // 친구 목록 화면
        FriendListScreen(
            friends = friends,
            menuTargetId = menuTargetId,
            onMoreClick = { friend -> menuTargetId = friend.id },
            onMenuDismiss = { menuTargetId = null },
            onBlockClick = { friend ->
                confirmTarget = friend
                menuTargetId = null
            },
            contentPaddingBottom =
                with(LocalDensity.current) {
                    WindowInsets.navigationBars.getBottom(this).toDp()
                },
        )
    }

    confirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmTarget = null },
            title = { Text("친구 차단하기") },
            text = { Text("${target.nickname} 을(를) 차단하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.blockFriend(target.id)
                        confirmTarget = null
                    },
                ) { Text("네") }
            },
            dismissButton = {
                TextButton(onClick = { confirmTarget = null }) { Text("아니오") }
            },
        )
    }
}

@Composable
private fun TopBar(
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO: 알림 이동 */ }) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = "알림")
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFD9D9D9)),
        placeholder = { Text("닉네임") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon =
            if (query.isNotEmpty()) {
                {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = "지우기")
                    }
                }
            } else null,
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFD9D9D9),
                unfocusedContainerColor = Color(0xFFD9D9D9),
                disabledContainerColor = Color(0xFFD9D9D9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
    )
}

/**
 * 친구 목록 화면
 */
@Composable
private fun FriendListScreen(
    friends: List<Friend>,
    menuTargetId: String?,
    onMoreClick: (Friend) -> Unit,
    onMenuDismiss: () -> Unit,
    onBlockClick: (Friend) -> Unit,
    contentPaddingBottom: Dp,
) {
    Surface(
        tonalElevation = 0.dp,
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = contentPaddingBottom + 12.dp),
        ) {
            items(friends) { friend ->
                FriendRow(
                    friend = friend,
                    menuOpen = menuTargetId == friend.id,
                    onMoreClick = onMoreClick,
                    onMenuDismiss = onMenuDismiss,
                    onBlockClick = onBlockClick,
                )
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun FriendRow(
    friend: Friend,
    menuOpen: Boolean,
    onMoreClick: (Friend) -> Unit,
    onMenuDismiss: () -> Unit,
    onBlockClick: (Friend) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF)) // color/background/whtie-primary
                    .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 왼쪽: 프로필 이미지 + 닉네임
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 프로필 이미지 (36x36, 원형)
                Box(
                    modifier =
                        Modifier
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
                    text = friend.nickname,
                    fontFamily = Pretendard,
                    fontSize = TypeScale.BodyM, // 16sp
                    fontWeight = FontWeight.Medium, // Medium
                    lineHeight = (TypeScale.BodyM.value * 1.5f).sp, // lineHeight 1.5
                    letterSpacing = (-0.16f).sp, // letterSpacing -0.16px
                    color = Color(0xFF191919), // color/text-border/primary
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 오른쪽: 더보기 아이콘
            Box {
                IconButton(onClick = { onMoreClick(friend) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "더보기",
                        tint = Color(0xFF818185), // grey
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = onMenuDismiss,
                ) {
                    DropdownMenuItem(
                        text = { Text("친구 차단하기") },
                        onClick = { onBlockClick(friend) },
                    )
                }
            }
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

@Preview(showBackground = true)
@Composable
private fun FriendScreenPreview() {
    WalkItTheme {
        FriendScreenPreviewContent()
    }
}

@Composable
private fun FriendScreenPreviewContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(onNavigateBack = {})
        SearchBar(query = "", onQueryChange = {}, onClear = {})
        FriendListScreen(
            friends =
                listOf(
                    Friend("1", "닉네임 01"),
                    Friend("2", "닉네임 02"),
                    Friend("3", "닉네임 03"),
                ),
            menuTargetId = null,
            onMoreClick = {},
            onMenuDismiss = {},
            onBlockClick = {},
            contentPaddingBottom = 0.dp,
        )
    }
}


