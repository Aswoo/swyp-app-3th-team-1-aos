package team.swyp.sdu.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 친구 요청 상태
 */
@Serializable
enum class FollowStatus {
    @SerialName("PENDING")
    PENDING, // 요청 대기 중
    
    @SerialName("FOLLOWING")
    FOLLOWING, // 친구 관계
    
    @SerialName("NONE")
    NONE, // 관계 없음
}

