package team.swyp.sdu.ui.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import team.swyp.sdu.core.Result
import team.swyp.sdu.data.remote.walking.mapper.FollowerWalkRecordMapper
import team.swyp.sdu.domain.model.FollowerWalkRecord
import team.swyp.sdu.domain.repository.WalkRepository
import javax.inject.Inject

/**
 * 친구 검색 상세 화면 ViewModel
 */
@HiltViewModel
class FriendSearchViewModel
    @Inject
    constructor(
        private val walkRepository: WalkRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<FriendSearchUiState>(FriendSearchUiState.Loading)
        val uiState: StateFlow<FriendSearchUiState> = _uiState.asStateFlow()

        /**
         * 팔로워 산책 기록 조회
         *
         * @param nickname 팔로워 닉네임 (null이면 내 최근 정보 조회)
         * @param lat 위도 (선택사항)
         * @param lon 경도 (선택사항)
         */
        fun loadFollowerWalkRecord(
            nickname: String,
            lat: Double? = null,
            lon: Double? = null,
        ) {
            viewModelScope.launch {
                _uiState.value = FriendSearchUiState.Loading
                withContext(Dispatchers.IO) {
                    val result = walkRepository.getFollowerWalkRecord(nickname, lat, lon)
                    
                    _uiState.value = when (result) {
                        is Result.Success -> {
                            val data = FollowerWalkRecordMapper.toDomain(result.data)
                            FriendSearchUiState.Success(data = data)
                        }
                        is Result.Error -> FriendSearchUiState.Error(result.message)
                        Result.Loading -> FriendSearchUiState.Loading
                    }
                }
            }
        }
    }


