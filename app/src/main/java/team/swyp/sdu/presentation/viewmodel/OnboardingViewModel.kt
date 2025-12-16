package team.swyp.sdu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import team.swyp.sdu.data.local.datastore.OnboardingDataStore
import java.time.LocalDate

/**
 * 온보딩 UI 상태
 */
data class OnboardingUiState(
    val currentStep: Int = 0,
    val serviceTermsChecked: Boolean = false,
    val privacyPolicyChecked: Boolean = false,
    val marketingConsentChecked: Boolean = false,
    val nickname: String = "",
    val goalCount: Int = 10,
    val stepTarget: Int = 0,
    val unit: String = "달",
    val birthYear: Int = LocalDate.now().year - 26,
    val birthMonth: Int = 1,
    val birthDay: Int = 1,
) {
    /**
     * 현재 단계에서 다음 버튼 활성화 여부
     */
    val canProceed: Boolean
        get() = when (currentStep) {
            0 -> serviceTermsChecked && privacyPolicyChecked
            1 -> nickname.trim().isNotEmpty()
            2 -> goalCount > 0 && stepTarget > 0
            3 -> {
                val yearValid = birthYear in 1901..LocalDate.now().year
                val monthValid = birthMonth in 1..12
                val dayValid = try {
                    LocalDate.of(birthYear, birthMonth, birthDay)
                    true
                } catch (e: Exception) {
                    false
                }
                yearValid && monthValid && dayValid
            }
            else -> false
        }
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val onboardingDataStore: OnboardingDataStore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        private val localCompleted = MutableStateFlow(false)

        val isCompleted: Flow<Boolean> =
            combine(onboardingDataStore.isCompleted, localCompleted) { fromDs, local ->
                fromDs || local
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

        /**
         * 현재 단계 변경
         */
        fun setStep(step: Int) {
            _uiState.value = _uiState.value.copy(currentStep = step)
        }

        /**
         * 다음 단계로 이동
         */
        fun nextStep() {
            val current = _uiState.value.currentStep
            if (current < 3 && _uiState.value.canProceed) {
                _uiState.value = _uiState.value.copy(currentStep = current + 1)
            }
        }

        /**
         * 이전 단계로 이동
         */
        fun previousStep() {
            val current = _uiState.value.currentStep
            if (current > 0) {
                _uiState.value = _uiState.value.copy(currentStep = current - 1)
            }
        }

        /**
         * 약관 동의 상태 업데이트
         */
        fun updateServiceTermsChecked(checked: Boolean) {
            _uiState.value = _uiState.value.copy(serviceTermsChecked = checked)
        }

        fun updatePrivacyPolicyChecked(checked: Boolean) {
            _uiState.value = _uiState.value.copy(privacyPolicyChecked = checked)
        }

        fun updateMarketingConsentChecked(checked: Boolean) {
            _uiState.value = _uiState.value.copy(marketingConsentChecked = checked)
        }

        /**
         * 닉네임 업데이트
         */
        fun updateNickname(nickname: String) {
            _uiState.value = _uiState.value.copy(nickname = nickname)
        }

        /**
         * 목표 설정 업데이트
         */
        fun updateGoalCount(goalCount: Int) {
            _uiState.value = _uiState.value.copy(goalCount = goalCount)
        }

        fun updateStepTarget(stepTarget: Int) {
            _uiState.value = _uiState.value.copy(stepTarget = stepTarget)
        }

        fun updateUnit(unit: String) {
            _uiState.value = _uiState.value.copy(unit = unit)
        }

        /**
         * 출생년도 업데이트
         */
        fun updateBirthYear(birthYear: Int) {
            _uiState.value = _uiState.value.copy(birthYear = birthYear)
        }

        /**
         * 출생월 업데이트
         */
        fun updateBirthMonth(birthMonth: Int) {
            _uiState.value = _uiState.value.copy(birthMonth = birthMonth)
        }

        /**
         * 출생일 업데이트
         */
        fun updateBirthDay(birthDay: Int) {
            _uiState.value = _uiState.value.copy(birthDay = birthDay)
        }

        /**
         * 온보딩 완료 및 서버 전송
         */
        fun submitOnboarding() {
            val state = _uiState.value
            viewModelScope.launch {
                try {
                    // TODO: 서버에 온보딩 데이터 전송
                    // 예시:
                    // val onboardingData = OnboardingData(
                    //     nickname = state.nickname,
                    //     goalCount = state.goalCount,
                    //     stepTarget = state.stepTarget,
                    //     unit = state.unit,
                    //     birthYear = state.birthYear,
                    //     marketingConsent = state.marketingConsentChecked
                    // )
                    // repository.submitOnboarding(onboardingData)

                    // 완료 상태 저장
                    localCompleted.value = true
                    onboardingDataStore.setCompleted(true)
                } catch (e: Exception) {
                    // TODO: 에러 처리
                    throw e
                }
            }
        }

        fun setCompleted() {
            localCompleted.value = true
            viewModelScope.launch {
                onboardingDataStore.setCompleted(true)
            }
        }
    }


