package team.swyp.sdu.data.dto.mission

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import team.swyp.sdu.domain.model.MissionConfig
import team.swyp.sdu.domain.model.MissionConfigParser
import team.swyp.sdu.domain.model.MissionType

@Keep
data class WeeklyMissionData(
    @SerializedName("userWeeklyMissionId")
    val userWeeklyMissionId: Long,

    @SerializedName("missionId")
    val missionId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("rewardPoints")
    val rewardPoints: Int,

    @SerializedName("assignedConfigJson")
    val assignedConfigJson: String,

    @SerializedName("weekStart")
    val weekStart: String,

    @SerializedName("weekEnd")
    val weekEnd: String,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("failedAt")
    val failedAt: String?,
) {
    /**
     * 미션 타입 enum으로 변환
     */
    fun getMissionType(): MissionType? {
        return MissionType.fromApiValue(type)
    }

    /**
     * 미션 설정 파싱
     */
    fun getMissionConfig(): MissionConfig? {
        return try {
            val missionType = getMissionType()
            missionType?.let { MissionConfigParser.parseMissionConfig(it, assignedConfigJson) }
        } catch (e: Exception) {
            null // 파싱 실패 시 null 반환
        }
    }

    companion object {
        val EMPTY = WeeklyMissionData(
            userWeeklyMissionId = 0L,
            missionId = 0L,
            title = "",
            description = "",
            category = "",
            type = "",
            status = "",
            rewardPoints = 0,
            assignedConfigJson = "{}",
            weekStart = "",
            weekEnd = "",
            completedAt = null,
            failedAt = null,
        )
    }
}
