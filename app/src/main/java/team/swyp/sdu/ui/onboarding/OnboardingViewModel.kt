package team.swyp.sdu.ui.onboarding

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import team.swyp.sdu.core.onError
import team.swyp.sdu.core.onSuccess
import team.swyp.sdu.data.local.datastore.OnboardingDataStore
import team.swyp.sdu.domain.model.OnboardingData
import team.swyp.sdu.domain.model.OnboardingProgress
import team.swyp.sdu.domain.model.Sex
import team.swyp.sdu.domain.repository.UserRepository
import timber.log.Timber
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
    val selectedImageUri: String? = null, // 선택된 이미지 URI
    val sex: Sex = Sex.MALE, // 성별
    val goalCount: Int = 10,
    val stepTarget: Int = 0,
    val unit: String = "주",
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
            2 -> {
                val yearValid = birthYear in 1901..LocalDate.now().year
                val monthValid = birthMonth in 1..12
                val dayValid = try {
                    LocalDate.of(birthYear, birthMonth, birthDay)
                    true
                } catch (e: Exception) {
                    false
                }
                val sexValid = sex != null
                yearValid && monthValid && dayValid && sexValid
            }
            3 -> {
                goalCount > 0 && stepTarget > 0
            }

            else -> false
        }

    /**
     * 생년월일 문자열 생성 (ISO 8601 형식)
     */
    val birthDateString: String
        get() = String.format("%04d-%02d-%02d", birthYear, birthMonth, birthDay)
}

@HiltViewModel
class OnboardingViewModel
@Inject
constructor(
    private val onboardingDataStore: OnboardingDataStore,
    private val userRepository: UserRepository,
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

    init {
        Timber.d("OnboardingViewModel init 시작")

        // 약관 동의 상태와 진행 상태를 함께 확인하여 적절한 단계로 이동
        viewModelScope.launch {
            val isTermsAgreed = onboardingDataStore.isTermsAgreed.first()
            val progress = onboardingDataStore.getProgress()

            Timber.d("온보딩 상태 확인 - 약관 동의: $isTermsAgreed, 진행 단계: ${progress.currentStep}")

            if (!isTermsAgreed) {
                // 약관 동의가 아직 안 된 경우
                Timber.d("약관 동의 단계부터 시작 (currentStep: 0)")
                // 기본 상태 (currentStep = 0) 유지
                return@launch
            }

            // 약관 동의가 완료된 경우, 진행된 단계에 따라 다음 단계로 이동
            if (progress.currentStep >= 3) {
                // 모든 단계 완료 - 온보딩 완료 처리
                Timber.d("모든 온보딩 단계 완료됨, 완료 상태 설정")
                localCompleted.value = true
                viewModelScope.launch {
                    onboardingDataStore.setCompleted(true)
                }
                // 완료된 경우 UI 상태를 업데이트하지 않음 (기본 상태 유지)
                return@launch
            }

            val targetStep = when {
                progress.currentStep == 2 -> {
                    // 목표 설정까지 완료 → 생년월일/성별 단계로 이동 (다음 단계)
                    Timber.d("목표 설정까지 완료됨 → 생년월일/성별 단계(3)로 이동")
                    3
                }
                progress.currentStep == 1 -> {
                    // 닉네임까지 완료 → 목표 설정 단계로 이동 (다음 단계)
                    Timber.d("닉네임까지 완료됨 → 목표 설정 단계(2)로 이동")
                    2
                }
                else -> {
                    // 약관 동의만 완료 → 닉네임 단계로 이동 (다음 단계)
                    Timber.d("약관 동의만 완료됨 → 닉네임 입력 단계(1)로 이동")
                    1
                }
            }

            _uiState.value = OnboardingUiState(
                currentStep = targetStep,
                serviceTermsChecked = true,
                privacyPolicyChecked = true,
                marketingConsentChecked = progress.marketingConsent,
                nickname = progress.nickname,
                selectedImageUri = progress.selectedImageUri,
                sex = progress.sex,
                goalCount = progress.goalCount,
                stepTarget = progress.stepTarget,
                unit = progress.unit,
                birthYear = progress.birthYear,
                birthMonth = progress.birthMonth,
                birthDay = progress.birthDay,
            )
        }
    }

    /**
     * 진행 상태 자동 저장
     */
    private fun saveProgress() {
        val currentState = _uiState.value
        val progress = OnboardingProgress(
            currentStep = currentState.currentStep,
            nickname = currentState.nickname,
            selectedImageUri = currentState.selectedImageUri,
            sex = currentState.sex,
            goalCount = currentState.goalCount,
            stepTarget = currentState.stepTarget,
            unit = currentState.unit,
            birthYear = currentState.birthYear,
            birthMonth = currentState.birthMonth,
            birthDay = currentState.birthDay,
            marketingConsent = currentState.marketingConsentChecked,
        )
        viewModelScope.launch {
            onboardingDataStore.saveProgress(progress)
        }
    }

    /**
     * 현재 단계 변경
     */
    fun setStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step)
        saveProgress()
    }

    /**
     * 다음 단계로 이동
     */
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < 3 && _uiState.value.canProceed) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
            saveProgress()
        }
    }

    /**
     * 이전 단계로 이동
     */
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1)
            saveProgress()
        }
    }

    /**
     * 약관 동의 상태 업데이트
     */
    fun updateServiceTermsChecked(checked: Boolean) {
        _uiState.value = _uiState.value.copy(serviceTermsChecked = checked)
        saveProgress()
        checkAndSaveTermsAgreement()
    }

    fun updatePrivacyPolicyChecked(checked: Boolean) {
        _uiState.value = _uiState.value.copy(privacyPolicyChecked = checked)
        saveProgress()
        checkAndSaveTermsAgreement()
    }

    fun updateMarketingConsentChecked(checked: Boolean) {
        _uiState.value = _uiState.value.copy(marketingConsentChecked = checked)
        saveProgress()
    }

    /**
     * 약관 동의 완료 상태 확인 및 저장
     */
    private fun checkAndSaveTermsAgreement() {
        val state = _uiState.value
        Timber.d("약관 동의 상태 확인: serviceTerms=${state.serviceTermsChecked}, privacyPolicy=${state.privacyPolicyChecked}")

        if (state.serviceTermsChecked && state.privacyPolicyChecked) {
            // 필수 약관들이 모두 동의된 경우 약관 동의 완료로 저장
            Timber.d("필수 약관 모두 동의됨, 약관 동의 완료 상태 저장")
            viewModelScope.launch {
                onboardingDataStore.setTermsAgreed(true)
                Timber.d("약관 동의 완료 상태 저장 - 성공")
            }
        } else {
            Timber.d("필수 약관이 아직 모두 동의되지 않음")
        }
    }

    /**
     * 약관 동의 완료 저장 (외부 호출용)
     */
    fun saveTermsAgreement() {
        Timber.d("saveTermsAgreement() 호출됨")
        viewModelScope.launch {
            onboardingDataStore.setTermsAgreed(true)
            Timber.d("약관 동의 완료 상태 저장 (명시적 호출) - 성공")
        }
    }

    /**
     * 닉네임 업데이트
     */
    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname)
        saveProgress()
    }

    /**
     * 목표 설정 업데이트
     */
    fun updateGoalCount(goalCount: Int) {
        _uiState.value = _uiState.value.copy(goalCount = goalCount)
        saveProgress()
    }

    fun updateStepTarget(stepTarget: Int) {
        _uiState.value = _uiState.value.copy(stepTarget = stepTarget)
        saveProgress()
    }

    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
        saveProgress()
    }

    /**
     * 출생년도 업데이트
     */
    fun updateBirthYear(birthYear: Int) {
        _uiState.value = _uiState.value.copy(birthYear = birthYear)
        saveProgress()
    }

    /**
     * 출생월 업데이트
     */
    fun updateBirthMonth(birthMonth: Int) {
        _uiState.value = _uiState.value.copy(birthMonth = birthMonth)
        saveProgress()
    }

    /**
     * 출생일 업데이트
     */
    fun updateBirthDay(birthDay: Int) {
        _uiState.value = _uiState.value.copy(birthDay = birthDay)
        saveProgress()
    }

    /**
     * 선택된 이미지 업데이트
     */
    fun updateSelectedImageUri(imageUri: String?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = imageUri)
        saveProgress()
    }

    /**
     * 성별 업데이트
     */
    fun updateSex(sex: Sex) {
        _uiState.value = _uiState.value.copy(sex = sex)
        saveProgress()
    }

    /**
     * 닉네임 등록
     */
    fun registerNickname() {
        val state = _uiState.value
        if (state.nickname.trim().isEmpty()) {
            Timber.w("닉네임이 비어있어 등록을 건너뜁니다")
            return
        }

        viewModelScope.launch {
            userRepository.registerNickname(state.nickname.trim())
                .onSuccess { user ->
                    Timber.d("닉네임 등록 성공: ${state.nickname}")
                    // 다음 단계로 진행
                    nextStep()
                }
                .onError { throwable, message ->
                    Timber.e(throwable, "닉네임 등록 실패: $message")
                    // TODO: 에러 처리 (UI에 에러 표시)
                }
        }
    }

    /**
     * 온보딩 완료 및 사용자 프로필 등록
     */
    fun submitOnboarding() {
        val state = _uiState.value

        // 필수 필드 검증
        if (state.sex == null) {
            Timber.e("성별이 선택되지 않았습니다")
            return
        }

        viewModelScope.launch {
            try {
                // PUT /users API 호출로 사용자 프로필 등록
                userRepository.updateUserProfile(
                    nickname = state.nickname,
                    birthDate = state.birthDateString,
                    sex = state.sex,
                    imageUri = state.selectedImageUri,
                ).onSuccess { user ->
                    Timber.d("사용자 프로필 등록 성공: ${user.nickname}")

                    // 온보딩 데이터 저장 (목표 설정 등)
                    val onboardingData = OnboardingData(
                        nickname = state.nickname,
                        goalCount = state.goalCount,
                        stepTarget = state.stepTarget,
                        unit = state.unit,
                        birthYear = state.birthYear,
                        marketingConsent = state.marketingConsentChecked
                    )
                    Timber.d("온보딩 데이터 저장: $onboardingData")

                    // 완료 상태 저장
                    localCompleted.value = true
                    onboardingDataStore.setCompleted(true)

                    // 모든 온보딩 데이터 초기화 (완료되었으므로 더 이상 필요 없음)
                    onboardingDataStore.clearAllOnboardingData()
                }.onError { throwable, message ->
                    Timber.e(throwable, "사용자 프로필 등록 실패: $message")
                    // TODO: 에러 처리 (UI에 에러 표시)
                }
            } catch (e: Exception) {
                Timber.e(e, "온보딩 완료 실패")
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


