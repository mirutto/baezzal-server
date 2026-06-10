package server.auth.implementation

import global.error.BadRequestException
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component

@Component
class Oauth2ClientRegistrationReader(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    fun read(provider: String): ClientRegistration =
        clientRegistrationRepository.findByRegistrationId(provider)
            ?: throw BadRequestException("지원하지 않는 OAuth provider 입니다")
}
