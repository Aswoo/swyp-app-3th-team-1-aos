package team.swyp.sdu.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoalInfo(
    @SerialName("period_type")
    val periodType: GoalPeriod = GoalPeriod.WEEK,
    @SerialName("target_sessions")
    val targetSessions: Int = 0,
    @SerialName("target_steps")
    val targetSteps: Int = 0,
)

@Serializable
enum class GoalPeriod {
    @SerialName("month")
    MONTH,

    @SerialName("week")
    WEEK,
}


