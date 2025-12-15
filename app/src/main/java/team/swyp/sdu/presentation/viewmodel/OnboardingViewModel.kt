package team.swyp.sdu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import team.swyp.sdu.data.local.datastore.OnboardingDataStore

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val onboardingDataStore: OnboardingDataStore,
    ) : ViewModel() {
        private val localCompleted = MutableStateFlow(false)

        val isCompleted: Flow<Boolean> =
            combine(onboardingDataStore.isCompleted, localCompleted) { fromDs, local ->
                fromDs || local
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

        fun setCompleted() {
            localCompleted.value = true
            viewModelScope.launch {
                onboardingDataStore.setCompleted(true)
            }
        }
    }


