package server.auth.application

import server.member.domain.MemberProvider
import server.member.domain.MemberRole

data class AuthTicketExchangeRequest(
    val ticket: String,
)

data class AuthTokenData(
    val accessToken: String,
    val refreshToken: String,
)

data class Oauth2Attributes(
    val provider: MemberProvider,
    val providerKey: String,
) {
    companion object {
        fun from(
            provider: MemberProvider,
            attributes: Map<String, Any>
        ): Oauth2Attributes = when (provider) {
            MemberProvider.GOOGLE -> fromGoogle(attributes)
            MemberProvider.KAKAO -> fromKakao(attributes)
            MemberProvider.NAVER -> fromNaver(attributes)
        }

        private fun fromGoogle(attr: Map<String, Any?>) = Oauth2Attributes(
            provider = MemberProvider.GOOGLE,
            providerKey = attr["sub"]?.toString()
                ?: throw IllegalStateException("Invalid google login response format"),
        )

        private fun fromNaver(attr: Map<String, Any?>): Oauth2Attributes {
            val response = attr["response"] as? Map<String, Any?>
                ?: throw IllegalStateException("Invalid naver login response format")

            return Oauth2Attributes(
                provider = MemberProvider.NAVER,
                providerKey = response["id"]?.toString()
                    ?: throw IllegalStateException("Invalid naver login response format"),
            )
        }

        private fun fromKakao(attr: Map<String, Any?>): Oauth2Attributes {
            val providerKey = attr["id"]?.toString()
                ?: throw IllegalStateException("Invalid kakao login response format")

            return Oauth2Attributes(
                provider = MemberProvider.KAKAO,
                providerKey = providerKey,
            )
        }
    }
}

data class MemberPrincipal(
    val memberId: Long,
    val role: MemberRole,
)
