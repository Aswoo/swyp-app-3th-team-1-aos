package team.swyp.sdu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사용자 캐시 Entity
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val nickname: String,
    val clearedCount: Int,
    val point: Int,
    val goalKmPerWeek: Double,
    val birthYear: Int? = null,
    val goalPeriodType: String? = null,
    val targetSessions: Int = 0,
    val targetSteps: Int = 0,
    val goalProgressSessions: Int = 0,
    val goalProgressSteps: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)


