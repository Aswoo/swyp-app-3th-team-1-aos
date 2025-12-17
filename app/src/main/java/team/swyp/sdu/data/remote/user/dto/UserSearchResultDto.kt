package team.swyp.sdu.data.remote.user.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import team.swyp.sdu.domain.model.FollowStatus

/**
 * 사용자 검색 결과 DTO
 *
 * 닉네임으로 사용자를 검색했을 때 반환되는 결과입니다.
 * Retrofit(Gson)과 kotlinx.serialization 모두 지원합니다.
 */
@Serializable
data class UserSearchResultDto(
    @SerializedName("userId")
    @SerialName("userId")
    val userId: Long,
    @SerializedName("imageName")
    @SerialName("imageName")
    val imageName: String? = null,
    @SerializedName("nickname")
    @SerialName("nickname")
    val nickname: String,
    @SerializedName("followStatus")
    @SerialName("followStatus")
    val followStatus: String, // PENDING, FOLLOWING, NONE
) {
    /**
     * followStatus를 FollowStatus enum으로 변환
     */
    fun getFollowStatusEnum(): FollowStatus {
        return try {
            FollowStatus.valueOf(followStatus)
        } catch (e: IllegalArgumentException) {
            FollowStatus.NONE
        }
    }
}

