package team.swyp.sdu.data.api.auth

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,   // "Bearer"
    val expiresIn: Long      // seconds (3600)
)