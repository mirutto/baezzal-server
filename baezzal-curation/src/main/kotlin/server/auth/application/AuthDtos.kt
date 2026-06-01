package server.auth.application

import global.error.InternalServerErrorException
import server.member.domain.MemberProvider

data class AuthTicketExchangeCommand(
    val ticket: String,
)

data class AuthTokenReissueCommand(
    val refreshToken: String,
)

data class AuthLogoutCommand(
    val refreshToken: String,
)

data class AuthTokenResult(
    val accessToken: String,
    val refreshToken: String,
)

data class AuthTicketExchangeResult(
    val accessToken: String,
    val refreshToken: String,
    val needsOnboarding: Boolean,
)

data class Oauth2Attributes(
    val provider: MemberProvider,
    val providerKey: String,
) {
    companion object {
        fun from(
            providerName: String,
            attributes: Map<String, Any>
        ): Oauth2Attributes {
            val provider = MemberProvider.from(providerName)

            return when (provider) {
                MemberProvider.GOOGLE -> fromGoogle(attributes)
                MemberProvider.KAKAO -> fromKakao(attributes)
                MemberProvider.NAVER -> fromNaver(attributes)
            }
        }

        private fun fromGoogle(attr: Map<String, Any?>) = Oauth2Attributes(
            provider = MemberProvider.GOOGLE,
            providerKey = attr["sub"]?.toString()
                ?: throw InternalServerErrorException("OAuth2 로그인 응답 형식이 올바르지 않습니다"),
        )

        private fun fromNaver(attr: Map<String, Any?>): Oauth2Attributes {
            val response = attr["response"] as? Map<String, Any?>
                ?: throw InternalServerErrorException("OAuth2 로그인 응답 형식이 올바르지 않습니다")

            return Oauth2Attributes(
                provider = MemberProvider.NAVER,
                providerKey = response["id"]?.toString()
                    ?: throw InternalServerErrorException("OAuth2 로그인 응답 형식이 올바르지 않습니다"),
            )
        }

        private fun fromKakao(attr: Map<String, Any?>): Oauth2Attributes {
            val providerKey = attr["id"]?.toString()
                ?: throw InternalServerErrorException("OAuth2 로그인 응답 형식이 올바르지 않습니다")

            return Oauth2Attributes(
                provider = MemberProvider.KAKAO,
                providerKey = providerKey,
            )
        }
    }
}

data class Oauth2LoginResult(
    val memberId: Long,
    val role: String,
)
