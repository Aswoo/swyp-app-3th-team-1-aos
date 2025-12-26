package team.swyp.sdu.ui.record.friendrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import team.swyp.sdu.core.Result
import team.swyp.sdu.data.remote.walking.mapper.FollowerWalkRecordMapper
import team.swyp.sdu.domain.model.FollowerWalkRecord
import team.swyp.sdu.domain.repository.WalkRepository
import timber.log.Timber
import java.util.LinkedHashMap
import javax.inject.Inject

private const val MAX_CACHE_SIZE = 5
private const val LIKE_DEBOUNCE_MS = 500L

// 한 팔로워의 산책 기록 하나만 캐시
data class FriendRecordState(
    val record: FollowerWalkRecord
)

@HiltViewModel
class FriendRecordViewModel @Inject constructor(
    private val walkRepository: WalkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FriendRecordUiState>(FriendRecordUiState.Loading)
    val uiState: StateFlow<FriendRecordUiState> = _uiState.asStateFlow()

    private var likeToggleJob: Job? = null

    // LRU 캐시: 최근 MAX_CACHE_SIZE명 저장
    private val friendStateCache: LinkedHashMap<String, FriendRecordState> =
        object : LinkedHashMap<String, FriendRecordState>(MAX_CACHE_SIZE + 1, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FriendRecordState>?): Boolean {
                return size > MAX_CACHE_SIZE
            }
        }

    /**
     * 팔로워 산책 기록 로드
     */
    fun loadFollowerWalkRecord(nickname: String) {
        viewModelScope.launch {
            // 1️⃣ 캐시 확인
            friendStateCache[nickname]?.let { cachedState ->
                _uiState.value = FriendRecordUiState.Success(
                    data = cachedState.record,
                    like = LikeUiState.EMPTY
                )
                return@launch
            }

            // 2️⃣ 로딩 상태
            _uiState.value = FriendRecordUiState.Loading

            // 3️⃣ 서버 요청
            val result = withContext(Dispatchers.IO) {
                walkRepository.getFollowerWalkRecord(nickname)
            }

            when (result) {
                is Result.Success -> {
                    val record = FollowerWalkRecordMapper.toDomain(result.data)

                    // 4️⃣ 캐시에 저장 (성공 시)
                    friendStateCache[nickname] = FriendRecordState(record)

                    // 5️⃣ UI 업데이트
                    _uiState.value = FriendRecordUiState.Success(
                        data = record,
                        like = LikeUiState.EMPTY
                    )
                }
                is Result.Error -> _uiState.value = FriendRecordUiState.Error(result.message)
                Result.Loading -> {} // 이미 Loading 상태
            }
        }
    }

    /**
     * 좋아요 토글 (Optimistic UI + debounce)
     */
    fun toggleLike() {
        val currentState = _uiState.value as? FriendRecordUiState.Success ?: return
        val walkId = currentState.data.walkId
        val isCurrentlyLiked = currentState.like.isLiked

        // 1️⃣ Optimistic UI 업데이트
        _uiState.value = currentState.copy(
            like = currentState.like.copy(
                isLiked = !isCurrentlyLiked,
                count = if (isCurrentlyLiked) (currentState.like.count - 1).coerceAtLeast(0)
                else currentState.like.count + 1
            )
        )

        // 2️⃣ debounce
        likeToggleJob?.cancel()
        likeToggleJob = viewModelScope.launch {
            delay(LIKE_DEBOUNCE_MS)
            val result = withContext(Dispatchers.IO) {
                if (isCurrentlyLiked) walkRepository.unlikeWalk(walkId)
                else walkRepository.likeWalk(walkId)
            }
        }
    }

    fun deleteFriend(nickname: String) {
        friendStateCache.remove(nickname)
    }
}
