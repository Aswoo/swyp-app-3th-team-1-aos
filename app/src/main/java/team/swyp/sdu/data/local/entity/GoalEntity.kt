package team.swyp.sdu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 목표 캐시 Entity
 *
 * 사용자의 산책 목표 정보를 저장합니다.
 * userId를 외래키로 사용하여 User와 연결됩니다.
 */
@Entity(tableName = "goal")
data class GoalEntity(
    @PrimaryKey
    val userId: Long,
    val targetStepCount: Int,
    val targetWalkCount: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)

