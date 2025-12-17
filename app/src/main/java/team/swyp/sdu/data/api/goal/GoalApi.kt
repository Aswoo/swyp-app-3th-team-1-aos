package team.swyp.sdu.data.api.goal

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import team.swyp.sdu.data.remote.goal.dto.RemoteGoalDto

/**
 * 목표 관련 API
 */
interface GoalApi {
    /**
     * 목표 설정
     *
     * @param goal 목표 데이터
     * @return 설정된 목표 데이터
     */
    @POST("/goals")
    suspend fun setGoal(@Body goal: RemoteGoalDto): RemoteGoalDto

    /**
     * 목표 조회
     *
     * @return 현재 설정된 목표 데이터
     */
    @GET("/goals")
    suspend fun getGoal(): RemoteGoalDto
}
