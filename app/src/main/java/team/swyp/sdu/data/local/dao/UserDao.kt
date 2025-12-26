package team.swyp.sdu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import team.swyp.sdu.data.local.entity.UserEntity

/**
 * 사용자 캐시 DAO
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserEntity)

    /**
     * 닉네임으로 프로필 이미지 업데이트
     *
     * @param nickname 사용자 닉네임
     * @param imageName 업데이트할 이미지 이름
     */
    @Query("UPDATE user_profile SET imageName = :imageName, updatedAt = :updatedAt WHERE nickname = :nickname")
    suspend fun updateImageNameByNickname(
        nickname: String,
        imageName: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM user_profile")
    suspend fun clear()
}
