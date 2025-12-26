package team.swyp.sdu.data.remote.walking.mapper

import team.swyp.sdu.data.remote.walking.dto.FollowerWalkRecordDto
import team.swyp.sdu.domain.model.Character
import team.swyp.sdu.domain.model.FollowerWalkRecord

/**
 * FollowerWalkRecord DTO → Domain Model 변환 매퍼
 */
object FollowerWalkRecordMapper {
    /**
     * FollowerWalkRecordDto → FollowerWalkRecord Domain Model
     */
    fun toDomain(dto: FollowerWalkRecordDto): FollowerWalkRecord {
        return FollowerWalkRecord(
            character = toDomain(dto.characterDto),
            walkProgressPercentage = dto.walkProgressPercentage ?: "0",
            createdDate = dto.createdDate ?: "0",
            stepCount = dto.stepCount,
            totalDistance = dto.totalDistance,
            likeCount = dto.likeCount,
            liked = dto.liked,
            walkId = dto.walkId ?: -1
        )
    }

    /**
     * CharacterDto → Character Domain Model
     */
    private fun toDomain(dto: team.swyp.sdu.data.remote.walking.dto.CharacterDto): Character {
        return Character(
            headImageName = dto.headImageName,
            bodyImageName = dto.bodyImageName,
            feetImageName = dto.feetImageName,
            characterImageName = dto.characterImageName,
            backgroundImageName = dto.backgroundImageName,
            level = dto.level,
            grade = dto.grade,
            nickName = dto.nickName ?: "게스트",
        )
    }
}

