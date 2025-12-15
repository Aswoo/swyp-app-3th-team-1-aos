package team.swyp.sdu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 미션 화면의 상태
 */
data class MissionUiState(
    val missions: List<MissionItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * 미션 아이템
 */
data class MissionItem(
    val id: String,
    val category: String,
    val title: String,
    val reward: String,
)

/**
 * 미션 ViewModel
 */
@HiltViewModel
class MissionViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        MissionUiState(
            missions = listOf(
                MissionItem(
                    id = "1",
                    category = "카테고리 1",
                    title = "5,000보 이상 걷기",
                    reward = "10 p",
                ),
                MissionItem(
                    id = "2",
                    category = "카테고리 1",
                    title = "5,000보 이상 걷기",
                    reward = "10 p",
                ),
                MissionItem(
                    id = "3",
                    category = "카테고리 1",
                    title = "5,000보 이상 걷기",
                    reward = "10 p",
                ),
            ),
        ),
    )
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
    }

    /**
     * 미션 목록 로드
     */
    private fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // TODO: API 호출로 미션 목록 가져오기
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }
}
