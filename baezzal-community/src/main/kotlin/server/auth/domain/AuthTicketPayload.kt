package server.auth.domain

data class AuthTicketPayload(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
)
