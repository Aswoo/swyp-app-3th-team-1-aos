package team.swyp.sdu.data.local.mapper

import team.swyp.sdu.data.local.entity.UserEntity
import team.swyp.sdu.domain.model.UserProfile
import team.swyp.sdu.domain.model.GoalInfo
import team.swyp.sdu.domain.model.GoalPeriod

/**
 * 사용자 캐시 매퍼
 */
object UserMapper {
    fun toEntity(domain: UserProfile): UserEntity =
        UserEntity(
            uid = domain.uid,
            nickname = domain.nickname,
            clearedCount = domain.clearedCount,
            point = domain.point,
            goalKmPerWeek = domain.goalKmPerWeek,
            birthYear = domain.birthYear,
            goalPeriodType = domain.goalInfo?.periodType?.name,
            targetSessions = domain.goalInfo?.targetSessions ?: 0,
            targetSteps = domain.goalInfo?.targetSteps ?: 0,
            goalProgressSessions = domain.goalProgressSessions,
            goalProgressSteps = domain.goalProgressSteps,
        )

    fun toDomain(entity: UserEntity): UserProfile =
        UserProfile(
            uid = entity.uid,
            nickname = entity.nickname,
            clearedCount = entity.clearedCount,
            point = entity.point,
            goalKmPerWeek = entity.goalKmPerWeek,
            birthYear = entity.birthYear,
            goalInfo =
                entity.goalPeriodType?.let { type ->
                    GoalInfo(
                        periodType = runCatching { GoalPeriod.valueOf(type) }.getOrDefault(GoalPeriod.WEEK),
                        targetSessions = entity.targetSessions,
                        targetSteps = entity.targetSteps,
                    )
                },
            goalProgressSessions = entity.goalProgressSessions,
            goalProgressSteps = entity.goalProgressSteps,
        )
}


