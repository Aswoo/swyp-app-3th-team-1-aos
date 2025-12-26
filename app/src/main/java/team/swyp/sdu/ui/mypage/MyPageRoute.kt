package team.swyp.sdu.ui.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import team.swyp.sdu.presentation.viewmodel.LoginViewModel

/**
 * 마이 페이지 Route
 *
 * ViewModel을 주입받고 상태를 수집하여 Screen에 전달합니다.
 */
@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    onNavigateCharacterEdit: () -> Unit = {},
    onNavigateUserInfoEdit: () -> Unit = {},
    onNavigateGoalManagement: () -> Unit = {},
    onNavigateNotificationSetting: () -> Unit = {},
    onNavigateMission: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPageScreen(
        uiState = uiState,
        onNavigateCharacterEdit = onNavigateCharacterEdit,
        onNavigateUserInfoEdit = onNavigateUserInfoEdit,
        onNavigateGoalManagement = onNavigateGoalManagement,
        onNavigateNotificationSetting = onNavigateNotificationSetting,
        onNavigateBack = onNavigateBack,
        onLogout = {
            loginViewModel.logout()
            onNavigateToLogin()
        },
        onNavigateMission = onNavigateMission,
        onWithdraw = {
            // TODO: 탈퇴 기능 구현
        },
    )
}


