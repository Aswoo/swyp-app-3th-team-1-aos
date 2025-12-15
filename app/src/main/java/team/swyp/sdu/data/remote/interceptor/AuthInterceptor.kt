package team.swyp.sdu.data.remote.interceptor

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

        val newRequest = if (!accessToken.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Timber.w("액세스 토큰이 없습니다. 요청: ${request.url}")
            request
        }

        return chain.proceed(newRequest)
    }
}

