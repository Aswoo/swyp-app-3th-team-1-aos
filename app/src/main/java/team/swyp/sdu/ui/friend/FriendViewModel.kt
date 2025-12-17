package team.swyp.sdu.ui.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import team.swyp.sdu.data.remote.user.UserRemoteDataSource
import team.swyp.sdu.data.remote.user.UserSearchResult
import team.swyp.sdu.domain.model.Friend
import timber.log.Timber
import javax.inject.Inject

/**
 * 검색 UI 상태
 */
sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val result: UserSearchResult) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class FriendViewModel
@Inject
constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
) : ViewModel() {

    private val _friends =
        MutableStateFlow(
            listOf(
                Friend("1", "닉네임"),
                Friend("2", "닉네임02"),
                Friend("3", "닉네임03"),
                Friend("4", "닉네임04"),
                Friend("5", "닉네임05"),
                Friend("6", "닉네임06"),
            ),
        )
    val friends: StateFlow<List<Friend>> = _friends

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState

    init {
        // 검색어 입력이 500ms 동안 멈추면 자동으로 검색 실행
        _query
            .debounce(500L)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { searchQuery ->
                searchUser(searchQuery)
            }
            .launchIn(viewModelScope)
    }

    val filteredFriends: StateFlow<List<Friend>> =
        combine(_friends, _query) { list, q ->
            val keyword = q.trim()
            if (keyword.isBlank()) list
            else list.filter { it.nickname.contains(keyword, ignoreCase = true) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = _friends.value,
        )

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
        _searchUiState.value = SearchUiState.Idle
    }

    /**
     * 닉네임으로 사용자 검색
     */
    fun searchUser(nickname: String) {
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            _searchUiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val result = userRemoteDataSource.searchUserByNickname(trimmedNickname)
                _searchUiState.value = SearchUiState.Success(result)
            } catch (e: Exception) {
                Timber.Forest.e(e, "사용자 검색 실패: $trimmedNickname")
                _searchUiState.value = SearchUiState.Error("검색 중 오류가 발생했습니다")
            }
        }
    }

    fun blockFriend(friendId: String) {
        _friends.update { current -> current.filterNot { it.id == friendId } }
    }
}