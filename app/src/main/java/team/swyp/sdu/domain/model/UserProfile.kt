package team.swyp.sdu.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 사용자 기본 정보 모델
 */
@Serializable
data class UserProfile(
    @SerialName("uid")
    val uid: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("cleared_count")
    val clearedCount: Int,
    @SerialName("point")
    val point: Int = 0,
    @SerialName("goal_km_per_week")
    val goalKmPerWeek: Double = 0.0,
    @SerialName("birth_year")
    val birthYear: Int? = null,
    @SerialName("goal_info")
    val goalInfo: GoalInfo? = null,
    @SerialName("goal_progress_sessions")
    val goalProgressSessions: Int = 0,
    @SerialName("goal_progress_steps")
    val goalProgressSteps: Int = 0,
) {
    companion object {
        val EMPTY =
            UserProfile(
                uid = "",
                nickname = "",
                clearedCount = 0,
                point = 0,
                goalKmPerWeek = 0.0,
                birthYear = null,
                goalInfo = null,
                goalProgressSessions = 0,
                goalProgressSteps = 0,
            )
    }
}


