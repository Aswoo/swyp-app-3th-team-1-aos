package team.swyp.sdu.data.api.user

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import team.swyp.sdu.data.remote.user.dto.RemoteUserDto
import team.swyp.sdu.data.remote.user.dto.UserSearchResultDto

/**
 * 사용자 정보 API
 */
interface UserApi {
    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GET("/users")
    suspend fun getUser(): RemoteUserDto

    /**
     * 닉네임으로 사용자 검색
     *
     * @param nickname 검색할 닉네임
     * @return 검색 결과 (사용자 정보 및 친구 요청 상태)
     */
    @GET("/users/nickname")
    suspend fun searchByNickname(
        @Query("nickname") nickname: String
    ): UserSearchResultDto

    /**
     * 닉네임 등록
     *
     * @param nickname 등록할 닉네임
     */
    @POST("/users/nickname/{nickname}")
    suspend fun registerNickname(
        @Path("nickname") nickname: String
    )

    /**
     * 사용자 정보 등록/업데이트 (온보딩 완료)
     *
     * @param image 프로필 이미지 (선택사항)
     * @param nickname 닉네임
     * @param birthDate 생년월일 (ISO 8601 형식)
     * @param sex 성별
     * @return 등록된 사용자 정보
     */
    @Multipart
    @PUT("/users")
    suspend fun updateUserProfile(
        @Part image: MultipartBody.Part? = null,
        @Part("nickname") nickname: RequestBody,
        @Part("birthDate") birthDate: RequestBody,
        @Part("sex") sex: RequestBody,
    ): RemoteUserDto
}

