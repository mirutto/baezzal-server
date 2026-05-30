package server.auth.domain

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
