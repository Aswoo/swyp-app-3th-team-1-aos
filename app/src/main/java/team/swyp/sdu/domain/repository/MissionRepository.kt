package team.swyp.sdu.domain.repository

import team.swyp.sdu.core.Result
import team.swyp.sdu.domain.model.WeeklyMission

/**
 * 미션 Repository 인터페이스
 */
interface MissionRepository {
    /**
     *  활성화된 주간 미션  조회
     */
    suspend fun getActiveWeeklyMission(): Result<List<WeeklyMission>>

    /**
     * 주간 미션 목록 조회
     */
    suspend fun getAllWeeklyMissions(): Result<List<WeeklyMission>>



}



