package team.swyp.sdu.data.remote.walking.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * 산책 저장 API 응답
 */
@Serializable
data class WalkSaveResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: WalkData? = null
)

/**
 * 저장된 산책 데이터
 */
@Serializable
data class WalkData(
    @SerializedName("id")
    val id: Long,

    @SerializedName("preWalkEmotion")
    val preWalkEmotion: String,

    @SerializedName("postWalkEmotion")
    val postWalkEmotion: String,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("startTime")
    val startTime: Long,

    @SerializedName("endTime")
    val endTime: Long,

    @SerializedName("stepCount")
    val stepCount: Int,

    @SerializedName("totalDistance")
    val totalDistance: Float,

    @SerializedName("createdDate")
    val createdDate: String,

    @SerializedName("points")
    val points: List<WalkPoint>
)

/**
 * 산책 포인트 데이터
 */
@Serializable
data class WalkPoint(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("timestampMillis")
    val timestampMillis: Long
)
