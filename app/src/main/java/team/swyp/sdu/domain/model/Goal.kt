package team.swyp.sdu.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 목표 도메인 모델
 *
 * 사용자의 산책 목표 정보를 포함합니다.
 * User와 완전히 분리된 독립적인 도메인 모델입니다.
 */
@Serializable
data class Goal(
    @SerialName("targetStepCount")
    val targetStepCount: Int,
    @SerialName("targetWalkCount")
    val targetWalkCount: Int,
) {
    companion object {
        val EMPTY = Goal(
            targetStepCount = 0,
            targetWalkCount = 0,
        )
    }
}

