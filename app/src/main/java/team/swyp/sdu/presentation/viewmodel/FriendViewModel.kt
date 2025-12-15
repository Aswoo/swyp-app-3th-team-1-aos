package team.swyp.sdu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import team.swyp.sdu.domain.model.Friend

@HiltViewModel
class FriendViewModel
    @Inject
    constructor() : ViewModel() {
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

        val filteredFriends: StateFlow<List<Friend>> =
            combine(_friends, _query) { list, q ->
                val keyword = q.trim()
                if (keyword.isBlank()) list
                else list.filter { it.nickname.contains(keyword, ignoreCase = true) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = _friends.value,
            )

        fun updateQuery(newQuery: String) {
            _query.value = newQuery
        }

        fun clearQuery() {
            _query.value = ""
        }

        fun blockFriend(friendId: String) {
            _friends.update { current -> current.filterNot { it.id == friendId } }
        }
    }


