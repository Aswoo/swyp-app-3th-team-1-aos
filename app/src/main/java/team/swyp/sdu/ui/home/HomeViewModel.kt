package team.swyp.sdu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import team.swyp.sdu.data.model.Emotion
import team.swyp.sdu.data.model.EmotionType
import team.swyp.sdu.data.model.WalkingSession
import team.swyp.sdu.data.repository.WalkingSessionRepository
import java.time.LocalDate
import java.time.ZoneId

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val sessionsThisWeek: List<WalkingSession>,
        val weeklyEmotionSummary: WeeklyEmotionSummary? = null,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * 이번 주 감정 요약
 */
data class WeeklyEmotionSummary(
    val mainEmotion: EmotionType,
    val mainEmotionCount: Int,
    val totalDays: Int,
    val positiveCount: Int,
    val negativeCount: Int,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walkingSessionRepository: WalkingSessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            walkingSessionRepository
                .getAllSessions()
                .catch { e ->
                    _uiState.value = HomeUiState.Error(e.message ?: "세션을 불러오지 못했습니다.")
                }
                .collect { sessions ->
                    val thisWeekSessions = sessions.filterThisWeek()
                    val emotionSummary = analyzeWeeklyEmotions(thisWeekSessions)
                    _uiState.value = HomeUiState.Success(
                        sessionsThisWeek = thisWeekSessions,
                        weeklyEmotionSummary = emotionSummary,
                    )
                }
        }
    }

    private fun List<WalkingSession>.filterThisWeek(): List<WalkingSession> {
        val today = LocalDate.now()
        val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(6)
        return filter { session ->
            val date =
                java.time.Instant.ofEpochMilli(session.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
        }.sortedByDescending { it.startTime }
    }

    /**
     * 이번 주 감정 분석
     */
    private fun analyzeWeeklyEmotions(sessions: List<WalkingSession>): WeeklyEmotionSummary? {
        // 모든 세션에서 감정 추출
        val allEmotions = sessions.flatMap { it.emotions }
        if (allEmotions.isEmpty()) return null

        // 감정 타입별 카운트
        val emotionCounts = allEmotions.groupingBy { it.type }.eachCount()

        // 긍정/부정 감정 분류
        val positiveEmotions = listOf(
            EmotionType.HAPPY,
            EmotionType.JOYFUL,
            EmotionType.EXCITED,
            EmotionType.THRILLED,
            EmotionType.PROUD,
            EmotionType.LIGHT_FOOTED,
            EmotionType.CALM,
            EmotionType.CONTENT,
            EmotionType.ENERGETIC,
            EmotionType.RELAXED,
        )

        val negativeEmotions = listOf(
            EmotionType.SAD,
            EmotionType.DEPRESSED,
            EmotionType.TIRED,
            EmotionType.SLUGGISH,
            EmotionType.MANY_THOUGHTS,
            EmotionType.COMPLEX_MIND,
            EmotionType.ANXIOUS,
        )

        val positiveCount = positiveEmotions.sumOf { emotionCounts[it] ?: 0 }
        val negativeCount = negativeEmotions.sumOf { emotionCounts[it] ?: 0 }

        // 가장 많이 나타난 감정 찾기
        val mainEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: EmotionType.HAPPY
        val mainEmotionCount = emotionCounts[mainEmotion] ?: 0

        // 이번 주 일수 계산 (월요일부터 오늘까지)
        val today = LocalDate.now()
        val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startOfWeek, today).toInt() + 1

        return WeeklyEmotionSummary(
            mainEmotion = mainEmotion,
            mainEmotionCount = mainEmotionCount,
            totalDays = totalDays,
            positiveCount = positiveCount,
            negativeCount = negativeCount,
        )
    }
}

