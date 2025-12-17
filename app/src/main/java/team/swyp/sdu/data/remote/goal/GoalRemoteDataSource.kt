package team.swyp.sdu.data.remote.goal

import javax.inject.Inject
import javax.inject.Singleton
import team.swyp.sdu.data.api.goal.GoalApi
import team.swyp.sdu.data.remote.goal.dto.RemoteGoalDto
import team.swyp.sdu.domain.model.Goal
import timber.log.Timber

/**
 * 목표 정보를 서버에서 가져오는 데이터 소스
 */
@Singleton
class GoalRemoteDataSource @Inject constructor(
    private val goalApi: GoalApi,
) {
    /**
     * 목표 조회 (GET /goals)
     */
    suspend fun fetchGoal(): Goal {
        return try {
            val dto = goalApi.getGoal()
            Timber.d("목표 조회 성공: 걸음=${dto.targetStepCount}, 산책=${dto.targetWalkCount}")
            dto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "목표 조회 실패")
            throw e
        }
    }

    /**
     * 목표 설정 (POST /goals)
     */
    suspend fun updateGoal(goal: Goal): Goal {
        return try {
            val dto = RemoteGoalDto(
                targetStepCount = goal.targetStepCount,
                targetWalkCount = goal.targetWalkCount,
            )
            val response = goalApi.setGoal(dto)
            Timber.d("목표 설정 성공: 걸음=${response.targetStepCount}, 산책=${response.targetWalkCount}")
            response.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "목표 설정 실패")
            throw e
        }
    }
}

