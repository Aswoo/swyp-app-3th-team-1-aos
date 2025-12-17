package team.swyp.sdu.data.remote.user.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import team.swyp.sdu.domain.model.User
import team.swyp.sdu.domain.model.Sex

/**
 * 사용자 API 응답 DTO
 *
 * 새로운 서버 API 구조에 맞춘 DTO입니다.
 * Retrofit(Gson)과 kotlinx.serialization 모두 지원합니다.
 */
@Serializable
data class RemoteUserDto(
    @SerializedName("userId")
    @SerialName("userId")
    val userId: Long,
    @SerializedName("imageName")
    @SerialName("imageName")
    val imageName: String? = null,
    @SerializedName("nickname")
    @SerialName("nickname")
    val nickname: String,
    @SerializedName("birthDate")
    @SerialName("birthDate")
    val birthDate: String?,
    @SerializedName("sex")
    @SerialName("sex")
    val sex: String? = null,
) {
    fun toDomain(): User = User(
        userId = userId,
        imageName = imageName,
        nickname = nickname,
        birthDate = birthDate,
        sex = sex?.let { 
            try {
                Sex.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        },
    )
}
