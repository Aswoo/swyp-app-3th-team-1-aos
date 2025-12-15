package team.swyp.sdu.data.remote.user.dto

import com.google.gson.annotations.SerializedName
import team.swyp.sdu.domain.model.GoalInfo
import team.swyp.sdu.domain.model.GoalPeriod

data class RemoteGoalDto(
    @SerializedName("period_type")
    val periodType: String? = null,
    @SerializedName("target_sessions")
    val targetSessions: Int = 0,
    @SerializedName("target_steps")
    val targetSteps: Int = 0,
    @SerializedName("progress_sessions")
    val progressSessions: Int = 0,
    @SerializedName("progress_steps")
    val progressSteps: Int = 0,
) {
    fun toGoalInfo(): GoalInfo =
        GoalInfo(
            periodType = runCatching { GoalPeriod.valueOf(periodType ?: "") }.getOrDefault(GoalPeriod.WEEK),
            targetSessions = targetSessions,
            targetSteps = targetSteps,
        )
}


