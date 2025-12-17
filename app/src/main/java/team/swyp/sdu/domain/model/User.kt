package team.swyp.sdu.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 사용자 도메인 모델
 *
 * 사용자의 기본 정보만 포함합니다.
 * Goal 정보는 별도의 Goal 모델로 분리되었습니다.
 */
@Serializable
data class User(
    @SerialName("userId")
    val userId: Long,
    @SerialName("imageName")
    val imageName: String? = null,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("birthDate")
    val birthDate: String?, // ISO 8601 형식: "2025-12-07"
    @SerialName("sex")
    val sex: Sex? = null,
) {
    companion object {
        val EMPTY = User(
            userId = 0,
            imageName = null,
            nickname = "",
            birthDate = "",
            sex = null,
        )
    }
}

/**
 * 성별 enum
 */
@Serializable
enum class Sex {
    @SerialName("MALE")
    MALE,
    
    @SerialName("FEMALE")
    FEMALE,
}

