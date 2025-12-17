package team.swyp.sdu.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import team.swyp.sdu.data.local.datastore.OnboardingDataStore
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore,
) : ViewModel() {

    val onboardingCompleted = onboardingDataStore.isCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(), // 타임아웃 제거
            initialValue = null, // 초기값을 null로 설정
        )
}
