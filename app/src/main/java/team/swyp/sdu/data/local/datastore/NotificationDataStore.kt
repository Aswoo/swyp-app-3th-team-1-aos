package team.swyp.sdu.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 알림 설정 보관용 DataStore
 *
 * FCM 알림 설정을 저장하고 관리합니다.
 */
@Singleton
class NotificationDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val notificationEnabledKey = booleanPreferencesKey("notification_enabled")
    private val goalNotificationEnabledKey = booleanPreferencesKey("goal_notification_enabled")
    private val newMissionNotificationEnabledKey = booleanPreferencesKey("new_mission_notification_enabled")
    private val friendRequestNotificationEnabledKey = booleanPreferencesKey("friend_request_notification_enabled")
    private val walkRecordNotificationEnabledKey = booleanPreferencesKey("walk_record_notification_enabled")

    val notificationEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[notificationEnabledKey] ?: true }
    val goalNotificationEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[goalNotificationEnabledKey] ?: true }
    val newMissionNotificationEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[newMissionNotificationEnabledKey] ?: true }
    val friendRequestNotificationEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[friendRequestNotificationEnabledKey] ?: true }
    val walkRecordNotificationEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[walkRecordNotificationEnabledKey] ?: true }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[notificationEnabledKey] = enabled }
    }

    suspend fun setGoalNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[goalNotificationEnabledKey] = enabled }
    }

    suspend fun setNewMissionNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[newMissionNotificationEnabledKey] = enabled }
    }

    suspend fun setFriendRequestNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[friendRequestNotificationEnabledKey] = enabled }
    }

    suspend fun setWalkRecordNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[walkRecordNotificationEnabledKey] = enabled }
    }
}

