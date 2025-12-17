package team.swyp.sdu.data.remote.user

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import team.swyp.sdu.data.api.user.UserApi
import team.swyp.sdu.data.remote.user.dto.UserSearchResultDto
import team.swyp.sdu.domain.model.FollowStatus
import team.swyp.sdu.domain.model.Sex
import team.swyp.sdu.domain.model.User
import timber.log.Timber
import java.io.File

/**
 * 사용자 정보를 서버에서 가져오는 데이터 소스
 */
@Singleton
class UserRemoteDataSource @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun fetchUser(): User {
        return try {
            val dto = userApi.getUser()
            Timber.d("사용자 정보 조회 성공: ${dto.nickname}")
            dto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "사용자 정보 조회 실패")
            throw e
        }
    }

    /**
     * 닉네임으로 사용자 검색
     *
     * @param nickname 검색할 닉네임
     * @return 검색 결과 (사용자 정보 및 친구 요청 상태)
     */
    suspend fun searchUserByNickname(nickname: String): UserSearchResult {
        return try {
            val dto = userApi.searchByNickname(nickname)
            Timber.d("사용자 검색 성공: ${dto.nickname}, 상태: ${dto.followStatus}")
            UserSearchResult(
                userId = dto.userId,
                imageName = dto.imageName,
                nickname = dto.nickname,
                followStatus = dto.getFollowStatusEnum(),
            )
        } catch (e: Exception) {
            Timber.e(e, "사용자 검색 실패: $nickname")
            throw e
        }
    }

    /**
     * 닉네임 등록
     *
     * @param nickname 등록할 닉네임
     */
    suspend fun registerNickname(nickname: String) {
        try {
            // 닉네임 등록 API 호출 (응답 본문 없음)
            userApi.registerNickname(nickname)
            Timber.d("닉네임 등록 성공: $nickname")
        } catch (e: Exception) {
            Timber.e(e, "닉네임 등록 실패: $nickname")
            throw e
        }
    }

    /**
     * 사용자 프로필 업데이트 (온보딩 완료)
     *
     * @param nickname 닉네임
     * @param birthDate 생년월일 (ISO 8601 형식)
     * @param sex 성별
     * @param imageUri 선택된 이미지 URI (선택사항)
     * @return 업데이트된 사용자 정보
     */
    suspend fun updateUserProfile(
        nickname: String,
        birthDate: String,
        sex: Sex,
        imageUri: String? = null,
    ): User {
        return try {
            // 이미지 파일 처리
            val imagePart = imageUri?.let { uri ->
                try {
                    val file = File(Uri.parse(uri).path ?: "")
                    if (file.exists()) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestFile)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Timber.w(e, "이미지 파일 처리 실패: $uri")
                    null
                }
            }

            // 텍스트 데이터들
            val nicknameBody = nickname.toRequestBody("text/plain".toMediaTypeOrNull())
            val birthDateBody = birthDate.toRequestBody("text/plain".toMediaTypeOrNull())
            val sexBody = sex.name.toRequestBody("text/plain".toMediaTypeOrNull())

            val dto = userApi.updateUserProfile(
                image = imagePart,
                nickname = nicknameBody,
                birthDate = birthDateBody,
                sex = sexBody,
            )

            Timber.d("사용자 프로필 업데이트 성공: $nickname")
            dto.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "사용자 프로필 업데이트 실패: $nickname")
            throw e
        }
    }
}

/**
 * 사용자 검색 결과 도메인 모델
 */
data class UserSearchResult(
    val userId: Long,
    val imageName: String?,
    val nickname: String,
    val followStatus: FollowStatus,
)
