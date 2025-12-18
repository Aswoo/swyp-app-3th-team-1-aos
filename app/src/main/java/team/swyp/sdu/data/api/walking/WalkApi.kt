package team.swyp.sdu.data.api.walking

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import team.swyp.sdu.data.remote.walking.dto.WalkSaveResponse

/**
 * 산책 관련 API
 */
interface WalkApi {

    /**
     * 산책 데이터 저장 (이미지 포함)
     *
     * @param data 산책 데이터 JSON
     * @param image 산책 이미지 (선택사항)
     * @return 저장 결과
     */
    @Multipart
    @POST("/walk/save")
    suspend fun saveWalk(
        @retrofit2.http.Part("data") data: RequestBody,
        @retrofit2.http.Part image: MultipartBody.Part?
    ): WalkSaveResponse
}
