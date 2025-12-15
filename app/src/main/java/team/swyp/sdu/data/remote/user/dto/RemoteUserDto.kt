package team.swyp.sdu.data.remote.user.dto

import com.google.gson.annotations.SerializedName
import team.swyp.sdu.domain.model.UserProfile

/**
 * 사용자 API 응답 DTO (스텁)
 */
data class RemoteUserDto(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("cleared_count")
    val clearedCount: Int,
    @SerializedName("point")
    val point: Int,
    @SerializedName("goal_km_per_week")
    val goalKmPerWeek: Double,
    @SerializedName("birth_year")
    val birthYear: Int? = null,
    @SerializedName("goal")
    val goal: RemoteGoalDto? = null,
) {
    fun toDomain(): UserProfile =
        UserProfile(
            uid = uid,
            nickname = nickname,
            clearedCount = clearedCount,
            point = point,
            goalKmPerWeek = goalKmPerWeek,
            birthYear = birthYear,
            goalInfo = goal?.toGoalInfo(),
            goalProgressSessions = goal?.progressSessions ?: 0,
            goalProgressSteps = goal?.progressSteps ?: 0,
        )
}


