package team.swyp.sdu.data.api.auth

data class SocialLoginRequest(
    val accessToken: String // 서버는 accessToken 기대
)