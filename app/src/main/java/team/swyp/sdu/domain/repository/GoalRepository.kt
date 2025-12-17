package team.swyp.sdu.domain.repository

import kotlinx.coroutines.flow.StateFlow
import team.swyp.sdu.core.Result
import team.swyp.sdu.domain.model.Goal

/**
 * 목표 정보 Repository 인터페이스
 */
interface GoalRepository {
    val goalFlow: StateFlow<Goal?>

    suspend fun getGoal(): Result<Goal>
    
    suspend fun updateGoal(userId: Long, goal: Goal): Result<Goal>
    
    suspend fun refreshGoal(): Result<Goal>
    
    suspend fun clearGoal(userId: Long): Result<Unit>
}

