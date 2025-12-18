package team.swyp.sdu.data.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import team.swyp.sdu.data.remote.auth.TokenProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 토큰을 요청 헤더에 추가하는 인터셉터
 * 
 * 주의: runBlocking을 사용하지 않고 TokenProvider의 캐시된 토큰 사용
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 인증이 필요 없는 요청은 제외
        // 1. 로그인 API는 토큰 불필요
        // 2. 공개 API는 필요시 추가
        if (request.url.encodedPath.contains("/auth/")) {
            return chain.proceed(request)
        }

        // 캐시된 토큰 가져오기 (동기, runBlocking 없음)
        val accessToken = tokenProvider.getAccessToken()
        Timber.d("AuthInterceptor - 요청 URL: ${request.url}, 토큰 존재: ${!accessToken.isNullOrBlank()}")

        val newRequest = if (!accessToken.isNullOrBlank()) {
            Timber.d("AuthInterceptor - Authorization 헤더 추가: Bearer ${accessToken.take(20)}...")
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Timber.w("액세스 토큰이 없습니다. 요청: ${request.url}")
            request
        }

        val response = chain.proceed(newRequest)

        // 응답 상태 로깅 (디버깅용)
        Timber.d("AuthInterceptor - 응답 상태: ${response.code}, Content-Type: ${response.header("Content-Type")}")

        // 인증 실패 감지: 302 리다이렉트 또는 HTML 응답
        val isAuthFailure = (response.code == 302 && response.header("Location")?.contains("/login") == true) ||
                           (response.header("Content-Type")?.contains("text/html") == true && !request.url.encodedPath.contains("/auth/"))

        // if (isAuthFailure) {
        //     Timber.e("AuthInterceptor - 인증 실패 감지! 코드: ${response.code}, Location: ${response.header("Location")}, URL: ${request.url}")
        //     Timber.e("AuthInterceptor - 토큰 클리어 실행")

        //     // 토큰 클리어 (동기 실행)
        //     runBlocking {
        //         try {
        //             tokenProvider.clearTokens()
        //             Timber.d("AuthInterceptor - 토큰 클리어됨")
        //         } catch (e: Exception) {
        //             Timber.e(e, "토큰 클리어 실패")
        //         }
        //     }

        //     // TODO: 로그인 화면으로 이동하는 이벤트 발생 (ViewModel이나 이벤트 버스 필요)
        //     // 예: EventBus.post(AuthenticationExpiredEvent())
        // }

        return response
    }
}


