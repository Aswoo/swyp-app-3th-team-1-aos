package team.swyp.sdu.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import team.swyp.sdu.MainActivity
import team.swyp.sdu.R
import timber.log.Timber
import javax.inject.Inject

/**
 * FCM 메시지 수신 및 토큰 갱신을 처리하는 서비스
 */
@AndroidEntryPoint
class WalkItFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CHANNEL_ID = "walkit_notification_channel"
        private const val CHANNEL_NAME = "알림"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * FCM 토큰 갱신 시 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM 토큰 갱신: $token")

        serviceScope.launch {
            fcmTokenManager.refreshToken(token)
        }
    }

    /**
     * FCM 메시지 수신 시 호출
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("FCM 메시지 수신: ${remoteMessage.messageId}")

        // 알림 표시
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "알림",
                body = notification.body ?: "",
                data = remoteMessage.data,
            )
        }
    }

    /**
     * 알림 표시
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 데이터 전달 (필요시)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 알림 아이콘으로 변경
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * NotificationChannel 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "앱 알림"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

