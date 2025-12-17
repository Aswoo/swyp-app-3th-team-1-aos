package team.swyp.sdu.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import team.swyp.sdu.core.onError
import team.swyp.sdu.core.onSuccess
import team.swyp.sdu.data.repository.WalkingSessionRepository
import team.swyp.sdu.domain.model.User
import team.swyp.sdu.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * 마이페이지 ViewModel
 * 누적 걸음수, 누적 이동거리, 사용자 정보를 관리합니다.
 */
@HiltViewModel
class MyPageViewModel
    @Inject
    constructor(
        private val walkingSessionRepository: WalkingSessionRepository,
        private val userRepository: UserRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
        val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

        init {
            loadData()
        }

        /**
         * 데이터 로드 (사용자 정보 + 누적 통계)
         */
        fun loadData() {
            viewModelScope.launch {
                // 사용자 정보 로드
                loadUser()
                
                // 누적 통계 로드
                loadSummary()
            }
        }

        /**
         * 사용자 정보 로드
         */
        private fun loadUser() {
            viewModelScope.launch {
                userRepository.getUser()
                    .onSuccess { user ->
                        val currentState = _uiState.value
                        _uiState.value = when (currentState) {
                            is MyPageUiState.Success -> currentState.copy(user = user)
                            else -> MyPageUiState.Success(
                                user = user,
                                totalStepCount = 0,
                                totalDistanceMeters = 0f,
                            )
                        }
                    }
                    .onError { exception, message ->
                        Timber.e(exception, "사용자 정보 로드 실패: $message")
                        // 사용자 정보 로드 실패해도 통계는 표시
                    }
            }
        }

        /**
         * 누적 통계 로드
         * Flow를 combine하여 총 걸음수와 총 거리를 함께 관리합니다.
         */
        private fun loadSummary() {
            viewModelScope.launch {
                combine(
                    walkingSessionRepository.getTotalStepCount(),
                    walkingSessionRepository.getTotalDistance(),
                ) { totalSteps, totalDistance ->
                    val currentState = _uiState.value
                    val currentUser = (currentState as? MyPageUiState.Success)?.user
                    MyPageUiState.Success(
                        user = currentUser,
                        totalStepCount = totalSteps,
                        totalDistanceMeters = totalDistance,
                    )
                }
                    .catch { e ->
                        Timber.e(e, "누적 통계 로드 실패")
                        val currentState = _uiState.value
                        val currentUser = (currentState as? MyPageUiState.Success)?.user
                        _uiState.value = MyPageUiState.Error(
                            user = currentUser,
                            message = e.message ?: "알 수 없는 오류가 발생했습니다",
                        )
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            }
        }
    }

/**
 * 마이페이지 UI 상태
 */
sealed class MyPageUiState {
    data object Loading : MyPageUiState()

    data class Success(
        val user: User? = null,
        val totalStepCount: Int,
        val totalDistanceMeters: Float,
    ) : MyPageUiState()

    data class Error(
        val user: User? = null,
        val message: String,
    ) : MyPageUiState()
}

