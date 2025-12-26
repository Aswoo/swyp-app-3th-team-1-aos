package team.swyp.sdu.ui.mission.model

import team.swyp.sdu.domain.model.WeeklyMission


enum class MissionCardState {
    INACTIVE,          // 대표 미션 아님 (조건 충족해도 리워드 불가)
    ACTIVE_CHALLENGE,  // 대표 미션 + 진행 중
    ACTIVE_REWARD,     // 대표 미션 + 보상 가능
    COMPLETED          // 리워드까지 완료
}

fun WeeklyMission.toCardState(isActive: Boolean): MissionCardState {
    if (!isActive) return MissionCardState.INACTIVE

    return when (status) {
        "IN_PROGRESS" -> MissionCardState.ACTIVE_CHALLENGE
        "COMPLETED" -> MissionCardState.ACTIVE_REWARD
        "REWARDED" -> MissionCardState.COMPLETED
        else -> MissionCardState.INACTIVE
    }
}