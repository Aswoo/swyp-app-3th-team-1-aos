package team.swyp.sdu.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 감정 데이터 모델
 *
 * @param type 감정 타입 (예: HAPPY, SAD, EXCITED, CALM, TIRED 등)
 * @param timestamp 감정 기록 시간 (밀리초)
 * @param note 감정에 대한 메모 (선택사항)
 */
@Parcelize
@Serializable
data class Emotion(
    val type: EmotionType,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
) : Parcelable

/**
 * 감정 타입 열거형
 */
@Serializable
enum class EmotionType {
    HAPPY,          // 기쁘다
    JOYFUL,         // 행복하다
    LIGHT_FOOTED,   // 발걸음이 가볍다
    EXCITED,        // 신난다
    THRILLED,       // 설레인다
    TIRED,          // 지친다
    SAD,            // 슬프다
    DEPRESSED,      // 우울하다
    SLUGGISH,       // 축축 처진다
    MANY_THOUGHTS,  // 생각이 많다
    COMPLEX_MIND,   // 머릿속이 복잡하다
    // 기존 감정 타입들 (하위 호환성 유지)
    CALM,           // 평온
    CONTENT,        // 만족
    ANXIOUS,        // 불안
    ENERGETIC,      // 활기참
    RELAXED,        // 편안함
    PROUD,          // 자랑스러움
}

