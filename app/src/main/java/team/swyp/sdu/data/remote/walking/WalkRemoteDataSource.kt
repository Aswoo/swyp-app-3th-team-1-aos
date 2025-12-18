package team.swyp.sdu.data.remote.walking

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import team.swyp.sdu.data.api.walking.WalkApi
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.data.remote.walking.dto.WalkSaveResponse
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.gson.Gson
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * 산책 데이터 서버 전송 데이터 소스
 */
@Singleton
class WalkRemoteDataSource @Inject constructor(
    private val walkApi: WalkApi,
    @ApplicationContext private val context: Context,
) {

    /**
     * 산책 데이터를 서버에 저장
     */
    suspend fun saveWalk(
        session: WalkingSession,
        imageUri: String? = null
    ): team.swyp.sdu.core.Result<WalkSaveResponse> {
        return try {
            // 산책 데이터를 JSON으로 변환
            val walkDataJson = createWalkDataJson(session)
            val dataBody = walkDataJson.toRequestBody("application/json".toMediaTypeOrNull())

            // 이미지 파일 처리
            val imagePart = imageUri?.let { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val file = uriToFile(uri, context)
                    if (file != null && file.exists()) {
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestFile)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Timber.w(e, "이미지 파일 처리 실패: $uriString")
                    null
                }
            }

            // API 호출
            val response = walkApi.saveWalk(
                data = dataBody,
                image = imagePart
            )

            Timber.d("산책 저장 성공: ${response.message}")
            team.swyp.sdu.core.Result.Success(response)

        } catch (e: Exception) {
            Timber.e(e, "산책 저장 실패")
            team.swyp.sdu.core.Result.Error(e, e.message)
        }
    }

    /**
     * WalkingSession을 API용 JSON 데이터로 변환
     */
    private fun createWalkDataJson(session: WalkingSession): String {
        val gson = Gson()

        // 감정 데이터 추출 (시작 전/후 감정)
        val preWalkEmotion = session.preWalkEmotion?.name ?: "HAPPY"
        val postWalkEmotion = session.postWalkEmotion?.name ?: "HAPPY"

        // GPS 포인트 변환
        val points = session.locations.map { location ->
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestampMillis" to location.timestamp
            )
        }

        // API 데이터 구조 생성
        val walkData = mapOf(
            "id" to 0, // 서버에서 생성
            "preWalkEmotion" to preWalkEmotion,
            "postWalkEmotion" to postWalkEmotion,
            "note" to (session.note ?: ""),
            "imageUrl" to (session.imageUrl ?: ""),
            "startTime" to session.startTime,
            "endTime" to (session.endTime ?: System.currentTimeMillis()),
            "stepCount" to session.stepCount,
            "totalDistance" to session.totalDistance,
            "createdDate" to (session.createdDate ?: ""),
            "points" to points
        )

        return gson.toJson(walkData)
    }

    /**
     * URI를 File로 변환
     */
    private fun uriToFile(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("walk_upload_", ".jpg", context.cacheDir)
            tempFile.deleteOnExit()

            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }

            tempFile
        } catch (e: Exception) {
            Timber.e(e, "URI를 File로 변환 실패: $uri")
            null
        }
    }
}
