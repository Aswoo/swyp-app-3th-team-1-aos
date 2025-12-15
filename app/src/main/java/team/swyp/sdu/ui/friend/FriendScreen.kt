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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.domain.model.Friend
import team.swyp.sdu.presentation.viewmodel.FriendViewModel
import team.swyp.sdu.ui.theme.WalkItTheme

@Composable
fun FriendScreen(
    onNavigateBack: () -> Unit,
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val friends by viewModel.filteredFriends.collectAsStateWithLifecycle()

    var menuTargetId by remember { mutableStateOf<String?>(null) }
    var confirmTarget by remember { mutableStateOf<Friend?>(null) }

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
            onClear = viewModel::clearQuery,
        )

        FriendList(
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
            IconButton(onClick = { /* TODO: 추가 액션 */ }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "검색")
            }
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

@Composable
private fun FriendList(
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
                Divider(color = Color(0xFFE0E0E0))
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
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4A4A)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🙂",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = friend.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "친구",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Box {
            IconButton(onClick = { onMoreClick(friend) }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "더보기")
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
        FriendList(
            friends =
                listOf(
                    Friend("1", "닉네임"),
                    Friend("2", "닉네임02"),
                    Friend("3", "닉네임03"),
                ),
            menuTargetId = null,
            onMoreClick = {},
            onMenuDismiss = {},
            onBlockClick = {},
            contentPaddingBottom = 0.dp,
        )
    }
}


