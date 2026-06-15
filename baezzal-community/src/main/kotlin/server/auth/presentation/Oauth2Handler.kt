package server.auth.presentation

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import server.auth.application.AuthService

@Component
class Oauth2Handler(
    private val authService: AuthService,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User = runCatching {
        val oAuth2User = delegate.loadUser(userRequest)
        val principal = authService.upsert(
            registrationId = userRequest.clientRegistration.registrationId,
            attributes = oAuth2User.attributes,
        )

        Oauth2SocialUser.Authenticated(
            memberId = principal.memberId,
            role = principal.role,
        )
    }.getOrElse {
        Oauth2SocialUser.HasError(it.message ?: "로그인을 할 수 없습니다.")
    }
}
