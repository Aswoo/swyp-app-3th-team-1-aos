package team.swyp.sdu.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.presentation.viewmodel.OnboardingViewModel
import team.swyp.sdu.ui.theme.WalkItTheme

@Composable
fun OnboardingScreen(
    onNavigateBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnitDropdown by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            when (uiState.currentStep) {
                0 -> TermsAgreementStep(
                    serviceTermsChecked = uiState.serviceTermsChecked,
                    privacyPolicyChecked = uiState.privacyPolicyChecked,
                    marketingConsentChecked = uiState.marketingConsentChecked,
                    onServiceTermsChecked = viewModel::updateServiceTermsChecked,
                    onPrivacyPolicyChecked = viewModel::updatePrivacyPolicyChecked,
                    onMarketingConsentChecked = viewModel::updateMarketingConsentChecked,
                    onNext = {
                        if (uiState.canProceed) {
                            viewModel.nextStep()
                        }
                    },
                    onNavigateBack = onNavigateBack,
                )
                1 -> NicknameStep(
                    value = uiState.nickname,
                    onChange = viewModel::updateNickname,
                    onNext = {
                        if (uiState.canProceed) {
                            viewModel.nextStep()
                        }
                    },
                    onPrev = viewModel::previousStep,
                )
                2 -> GoalStep(
                    unit = uiState.unit,
                    unitMenuExpanded = showUnitDropdown,
                    onUnitClick = { showUnitDropdown = true },
                    onUnitDismiss = { showUnitDropdown = false },
                    onUnitSelect = {
                        viewModel.updateUnit(it)
                        showUnitDropdown = false
                    },
                    goal = uiState.goalCount,
                    onGoalChange = viewModel::updateGoalCount,
                    steps = uiState.stepTarget,
                    onStepsChange = viewModel::updateStepTarget,
                    onNext = {
                        if (uiState.canProceed) {
                            viewModel.nextStep()
                        }
                    },
                    onPrev = viewModel::previousStep,
                )
                3 -> BirthYearStep(
                    currentYear = uiState.birthYear,
                    currentMonth = uiState.birthMonth,
                    currentDay = uiState.birthDay,
                    onYearChange = viewModel::updateBirthYear,
                    onMonthChange = viewModel::updateBirthMonth,
                    onDayChange = viewModel::updateBirthDay,
                    onNext = {
                        if (uiState.canProceed) {
                            viewModel.submitOnboarding()
                            onFinish()
                        }
                    },
                    onPrev = viewModel::previousStep,
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    WalkItTheme {
        OnboardingScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenStep0Preview() {
    WalkItTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TermsAgreementStep(
                serviceTermsChecked = true,
                privacyPolicyChecked = true,
                marketingConsentChecked = false,
                onServiceTermsChecked = {},
                onPrivacyPolicyChecked = {},
                onMarketingConsentChecked = {},
                onNext = {},
                onNavigateBack = {},
            )
        }
    }
}




