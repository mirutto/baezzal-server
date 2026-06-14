package server.token

data class AuthPrincipal(
    val memberId: Long,
    val type: TokenType,
    val role: String? = null,
    val sessionId: String? = null,
) {
    companion object {
        fun accessToken(
            memberId: Long,
            roles: String,
        ) = AuthPrincipal(
            memberId = memberId,
            type = TokenType.ACCESS,
            role = roles,
        )

        fun refreshToken(
            memberId: Long,
            sessionId: String,
        ) = AuthPrincipal(
            memberId = memberId,
            type = TokenType.REFRESH,
            sessionId = sessionId,
        )
    }
}
