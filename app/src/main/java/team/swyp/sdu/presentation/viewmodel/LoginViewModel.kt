package team.swyp.sdu.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import team.swyp.sdu.core.Result
import team.swyp.sdu.data.local.datastore.AuthDataStore
import team.swyp.sdu.data.remote.auth.AuthRemoteDataSource
import team.swyp.sdu.data.remote.auth.TokenProvider
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * 로그인 상태
 */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val token: OAuthToken) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * 로그인 ViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authDataStore: AuthDataStore,
    private val tokenProvider: TokenProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoginChecked = MutableStateFlow(false)
    val isLoginChecked: StateFlow<Boolean> = _isLoginChecked.asStateFlow()

    init {
        checkLoginStatus()
    }

    /**
     * 로그인 상태 확인
     * 서버 토큰(AuthDataStore)을 기준으로 확인
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoginChecked.value = false
            try {
                // 서버 토큰 확인 (AuthDataStore)
                val accessToken = authDataStore.accessToken.first()
                if (!accessToken.isNullOrBlank()) {
                    // 서버 토큰이 있으면 로그인 상태로 간주
                    _isLoggedIn.value = true
                    Timber.i("서버 토큰 확인됨 - 로그인 상태")
                } else {
                    // 서버 토큰이 없으면 로그인 안 된 상태
                    _isLoggedIn.value = false
                    Timber.i("서버 토큰 없음 - 로그인 필요")
                }
            } catch (e: Exception) {
                Timber.e(e, "로그인 상태 확인 실패")
                _isLoggedIn.value = false
            } finally {
                _isLoginChecked.value = true
            }
        }
    }

    /**
     * 카카오톡으로 로그인
     */
    fun loginWithKakaoTalk(context: Context) {
        _uiState.value = LoginUiState.Loading

        // 카카오톡 로그인 가능 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Timber.e(error, "카카오톡으로 로그인 실패")

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        _uiState.value = LoginUiState.Error("로그인이 취소되었습니다.")
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    loginWithKakaoAccount(context)
                } else if (token != null) {
                    Timber.i("카카오톡으로 로그인 성공 ${token.accessToken}")
                    // 서버에 토큰 전송
                    sendTokenToServer(token.accessToken, isKakao = true)
                }
            }
        } else {
            // 카카오톡이 설치되어 있지 않으면 카카오계정으로 로그인
            loginWithKakaoAccount(context)
        }
    }

    /**
     * 카카오계정으로 로그인
     */
    fun loginWithKakaoAccount(context: Context) {
        _uiState.value = LoginUiState.Loading

        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Timber.e(error, "카카오계정으로 로그인 실패")
                _uiState.value = LoginUiState.Error("로그인에 실패했습니다: ${error.message}")
            } else if (token != null) {
                Timber.i("카카오계정으로 로그인 성공 ${token.accessToken}")
                // 서버에 토큰 전송
                sendTokenToServer(token.accessToken, isKakao = true)
            }
        }
    }

    /**
     * 네이버 로그인 (ActivityResultLauncher 사용)
     */
    fun loginWithNaver(context: Context, launcher: ActivityResultLauncher<Intent>) {
        _uiState.value = LoginUiState.Loading
        NidOAuth.requestLogin(context, launcher)
    }

    /**
     * 네이버 로그인 결과 처리
     */
    fun handleNaverLoginResult(result: ActivityResult) {
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                // 네이버 로그인 성공
                val accessToken = NidOAuth.getAccessToken()
                if (accessToken != null) {
                    Timber.i("네이버 로그인 성공: $accessToken")
                    // 서버에 토큰 전송
                    sendTokenToServer(accessToken, isKakao = false)
                } else {
                    _uiState.value = LoginUiState.Error("토큰을 가져오지 못했습니다.")
                }
            }
            Activity.RESULT_CANCELED -> {
                // 로그인 실패 또는 취소
                val errorCode = NidOAuth.getLastErrorCode().code
                val errorDescription = NidOAuth.getLastErrorDescription()
                Timber.e("네이버 로그인 실패: $errorCode - $errorDescription")
                _uiState.value = LoginUiState.Error("로그인에 실패했습니다: $errorDescription")
            }
        }
    }

    /**
     * 네이버 로그인 (Callback 사용)
     */
    fun loginWithNaver(context: Context) {
        _uiState.value = LoginUiState.Loading

        val nidOAuthCallback = object : NidOAuthCallback {
            override fun onSuccess() {
                val accessToken = NidOAuth.getAccessToken()
                if (accessToken != null) {
                    Timber.i("네이버 로그인 성공: $accessToken")
                    // 서버에 토큰 전송
                    sendTokenToServer(accessToken, isKakao = false)
                } else {
                    _uiState.value = LoginUiState.Error("토큰을 가져오지 못했습니다.")
                }
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                Timber.e("네이버 로그인 실패: $errorCode - $errorDesc")
                _uiState.value = LoginUiState.Error("로그인에 실패했습니다: $errorDesc")
            }
        }

        NidOAuth.requestLogin(context, nidOAuthCallback)
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            // 카카오 로그아웃
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Timber.e(error, "카카오 로그아웃 실패")
                } else {
                    Timber.i("카카오 로그아웃 성공")
                }
            }

            // 네이버 로그아웃
            val naverCallback = object : NidOAuthCallback {
                override fun onSuccess() {
                    Timber.i("네이버 로그아웃 성공")
                }

                override fun onFailure(errorCode: String, errorDesc: String) {
                    Timber.e("네이버 로그아웃 실패: $errorCode - $errorDesc")
                }
            }
            NidOAuth.logout(naverCallback)

            // 토큰 삭제
            tokenProvider.clearTokens()
            authDataStore.clear()

            _isLoggedIn.value = false
            _uiState.value = LoginUiState.Idle
        }
    }

    /**
     * 소셜 로그인 토큰을 서버에 전송하고 서버 토큰 받기
     */
    private fun sendTokenToServer(socialAccessToken: String, isKakao: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading

                val result = if (isKakao) {
                    authRemoteDataSource.loginWithKakao(socialAccessToken)
                } else {
                    authRemoteDataSource.loginWithNaver(socialAccessToken)
                }

                when (result) {
                    is Result.Success -> {
                        val tokenResponse = result.data
                        // 서버 토큰 저장
                        authDataStore.saveTokens(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken,
                        )
                        // TokenProvider도 업데이트 (Flow 구독으로 자동 업데이트되지만 명시적으로 호출)
                        tokenProvider.updateTokens(
                            tokenResponse.accessToken,
                            tokenResponse.refreshToken,
                        )

                        _isLoggedIn.value = true
                        _uiState.value = LoginUiState.Idle
                        Timber.i("서버 로그인 성공")
                    }
                    is Result.Error -> {
                        _uiState.value = LoginUiState.Error(
                            result.message ?: "서버 로그인에 실패했습니다",
                        )
                        Timber.e(result.exception, "서버 로그인 실패")
                    }
                    Result.Loading -> {
                        // 이미 Loading 상태
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("로그인 처리 중 오류 발생: ${e.message}")
                Timber.e(e, "로그인 처리 실패")
            }
        }
    }
}

