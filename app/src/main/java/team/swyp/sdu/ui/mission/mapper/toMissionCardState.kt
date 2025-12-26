package team.swyp.sdu.ui.mission.mapper

import team.swyp.sdu.domain.model.WeeklyMission
import team.swyp.sdu.ui.mission.model.MissionCardState

fun WeeklyMission.toMissionCardState(
    isActive: Boolean
): MissionCardState {
    if (!isActive) return MissionCardState.INACTIVE

    return when (status) {
        "IN_PROGRESS" -> MissionCardState.ACTIVE_CHALLENGE
        "COMPLETED" -> MissionCardState.ACTIVE_REWARD
        "REWARDED" -> MissionCardState.COMPLETED
        else -> MissionCardState.INACTIVE
    }
}