package team.swyp.sdu

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.core.data.datastore.NidOAuthInitializingCallback
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import team.swyp.sdu.data.remote.billing.BillingManager
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WalkingBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Timber 초기화
        // 개발 중이므로 항상 DebugTree 사용
        // 릴리즈 빌드에서는 Crashlytics 등으로 로그 전송 가능
        Timber.plant(Timber.DebugTree())

        // Kakao SDK 초기화
        // AndroidManifest.xml의 meta-data에서 앱 키를 읽어옴
        val kakaoAppKey = "42a206965aff02858c74267290ba5ab6"
        KakaoSdk.init(this, kakaoAppKey)

        // KakaoMap SDK 초기화
        KakaoMapSdk.init(this, kakaoAppKey)

        // Naver OAuth SDK 초기화
        val naverClientId = "pqYCAiLlppKm8_M3VnNA"
        val naverClientSecret = "V_NACUpG7I"
        val naverClientName = "walkit"
        NidOAuth.initialize(
            this,
            naverClientId,
            naverClientSecret,
            naverClientName,
            object : NidOAuthInitializingCallback {
                override fun onSuccess() {
                    Timber.d("Naver OAuth SDK 초기화 성공")
                }

                override fun onFailure(e: Exception) {
                    Timber.e(e, "Naver OAuth SDK 초기화 실패")
                }
            },
        )

        // Google Play Billing 초기화
        // Hilt가 완전히 초기화된 후에 주입받아야 하므로 EntryPoint 사용
        val entryPoint = EntryPoints.get(this, BillingEntryPoint::class.java)
        val billingManager = entryPoint.billingManager()
        billingManager.initialize()
        Timber.d("Google Play Billing 초기화 완료")

        Timber.d("WalkingBuddyApplication onCreate")
    }

    /**
     * BillingManager를 주입받기 위한 EntryPoint
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BillingEntryPoint {
        fun billingManager(): BillingManager
    }
}
