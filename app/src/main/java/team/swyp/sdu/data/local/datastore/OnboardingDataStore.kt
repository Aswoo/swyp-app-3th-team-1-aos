package team.swyp.sdu.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OnboardingDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val completedKey = booleanPreferencesKey("onboarding_completed")

    val isCompleted: Flow<Boolean> = dataStore.data.map { prefs -> prefs[completedKey] ?: false }

    suspend fun setCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[completedKey] = completed }
    }
}


