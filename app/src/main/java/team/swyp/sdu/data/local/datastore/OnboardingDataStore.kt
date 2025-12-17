package team.swyp.sdu.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import team.swyp.sdu.domain.model.OnboardingProgress
import team.swyp.sdu.domain.model.Sex

@Singleton
class OnboardingDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    // 기존 키들
    private val completedKey = booleanPreferencesKey("onboarding_completed")

    // 약관 동의 완료 상태
    private val termsAgreedKey = booleanPreferencesKey("onboarding_terms_agreed")

    // 진행 상태를 위한 개별 키들
    private val currentStepKey = intPreferencesKey("onboarding_current_step")
    private val nicknameKey = stringPreferencesKey("onboarding_nickname")
    private val selectedImageUriKey = stringPreferencesKey("onboarding_selected_image_uri")
    private val sexKey = stringPreferencesKey("onboarding_sex")
    private val goalCountKey = intPreferencesKey("onboarding_goal_count")
    private val stepTargetKey = intPreferencesKey("onboarding_step_target")
    private val unitKey = stringPreferencesKey("onboarding_unit")
    private val birthYearKey = intPreferencesKey("onboarding_birth_year")
    private val birthMonthKey = intPreferencesKey("onboarding_birth_month")
    private val birthDayKey = intPreferencesKey("onboarding_birth_day")
    private val marketingConsentKey = booleanPreferencesKey("onboarding_marketing_consent")

    val isCompleted: Flow<Boolean> = dataStore.data.map { prefs -> prefs[completedKey] ?: false }

    val isTermsAgreed: Flow<Boolean> = dataStore.data.map { prefs -> prefs[termsAgreedKey] ?: false }

    suspend fun getProgress(): OnboardingProgress {
        return dataStore.data.first().let { prefs ->
            OnboardingProgress(
                currentStep = prefs[currentStepKey] ?: 0,
                nickname = prefs[nicknameKey] ?: "",
                selectedImageUri = prefs[selectedImageUriKey],
                sex = when (prefs[sexKey]) {
                    "FEMALE" -> Sex.FEMALE
                    else -> Sex.MALE
                },
                goalCount = prefs[goalCountKey] ?: 10,
                stepTarget = prefs[stepTargetKey] ?: 0,
                unit = prefs[unitKey] ?: "달",
                birthYear = prefs[birthYearKey] ?: 1990,
                birthMonth = prefs[birthMonthKey] ?: 1,
                birthDay = prefs[birthDayKey] ?: 1,
                marketingConsent = prefs[marketingConsentKey] ?: false
            )
        }
    }

    suspend fun setCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[completedKey] = completed }
    }

    suspend fun setTermsAgreed(agreed: Boolean) {
        dataStore.edit { prefs -> prefs[termsAgreedKey] = agreed }
    }

    suspend fun saveProgress(progress: OnboardingProgress) {
        dataStore.edit { prefs ->
            prefs[currentStepKey] = progress.currentStep
            prefs[nicknameKey] = progress.nickname
            progress.selectedImageUri?.let { prefs[selectedImageUriKey] = it }
            prefs[sexKey] = progress.sex.name
            prefs[goalCountKey] = progress.goalCount
            prefs[stepTargetKey] = progress.stepTarget
            prefs[unitKey] = progress.unit
            prefs[birthYearKey] = progress.birthYear
            prefs[birthMonthKey] = progress.birthMonth
            prefs[birthDayKey] = progress.birthDay
            prefs[marketingConsentKey] = progress.marketingConsent
        }
    }

    suspend fun clearProgress() {
        dataStore.edit { prefs ->
            prefs.remove(currentStepKey)
            prefs.remove(nicknameKey)
            prefs.remove(selectedImageUriKey)
            prefs.remove(sexKey)
            prefs.remove(goalCountKey)
            prefs.remove(stepTargetKey)
            prefs.remove(unitKey)
            prefs.remove(birthYearKey)
            prefs.remove(birthMonthKey)
            prefs.remove(birthDayKey)
            prefs.remove(marketingConsentKey)
        }
    }

    suspend fun clearAllOnboardingData() {
        dataStore.edit { prefs ->
            // 약관 동의 상태도 초기화
            prefs.remove(termsAgreedKey)
            clearProgress()
        }
    }
}


