package team.swyp.sdu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import team.swyp.sdu.data.local.entity.GoalEntity

/**
 * 목표 캐시 DAO
 */
@Dao
interface GoalDao {
    @Query("SELECT * FROM goal WHERE userId = :userId LIMIT 1")
    fun observeGoal(userId: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goal WHERE userId = :userId LIMIT 1")
    suspend fun getGoalByUserId(userId: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GoalEntity)

    @Query("DELETE FROM goal WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Long)

    @Query("DELETE FROM goal")
    suspend fun clear()
}

