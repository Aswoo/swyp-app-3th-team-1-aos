package team.swyp.sdu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 목표 관리 화면의 상태
 */
data class GoalState(
    val targetSteps: Int = 10000,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, 7)
    }.timeInMillis,
    val walkFrequency: Int = 3,
    val missionSuccessCount: Int = 0,
)

/**
 * 목표 관리 ViewModel
 */
@HiltViewModel
class GoalManagementViewModel @Inject constructor() : ViewModel() {
    private val _goalState = MutableStateFlow(
        GoalState(
            targetSteps = 10000,
            startDate = System.currentTimeMillis(),
            endDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 7)
            }.timeInMillis,
            walkFrequency = 3,
            missionSuccessCount = 0,
        ),
    )
    val goalState: StateFlow<GoalState> = _goalState.asStateFlow()

    /**
     * 목표 업데이트
     */
    fun updateGoal(
        targetSteps: Int,
        startDate: Long,
        endDate: Long,
        walkFrequency: Int,
        missionSuccessCount: Int,
    ) {
        viewModelScope.launch {
            _goalState.value = GoalState(
                targetSteps = targetSteps,
                startDate = startDate,
                endDate = endDate,
                walkFrequency = walkFrequency,
                missionSuccessCount = missionSuccessCount,
            )
            // TODO: 서버에 저장
        }
    }

    /**
     * 목표 초기화
     */
    fun resetGoal() {
        viewModelScope.launch {
            _goalState.value = GoalState(
                targetSteps = 10000,
                startDate = System.currentTimeMillis(),
                endDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 7)
                }.timeInMillis,
                walkFrequency = 3,
                missionSuccessCount = 0,
            )
            // TODO: 서버에 저장
        }
    }
}
