package server.auth.presentation

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import server.auth.application.AuthService
import server.auth.infrastructure.Oauth2SocialUser

@Component
class Oauth2SuccessHandler(
    private val authService: AuthService,
    private val environment: Environment,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val authenticatedUser = authentication.principal as Oauth2SocialUser
        val secure = request.isSecure

        val redirectUrl = when (authenticatedUser) {
            is Oauth2SocialUser.Authenticated -> {
                val ticket = authService.issueLoginTicket(
                    memberId = authenticatedUser.memberId,
                    role = authenticatedUser.role,
                )

                request.resolveOauth2RedirectUrl(
                    "type" to "success",
                    "ticket" to ticket,
                )
            }

            is Oauth2SocialUser.HasError -> {
                request.resolveOauth2RedirectUrl(
                    "type" to "hasError",
                    "errorMessage" to authenticatedUser.message,
                )
            }
        }

        response.expireOauth2RedirectUriCookie(secure = secure)
        response.status = HttpStatus.FOUND.value()
        response.sendRedirect(redirectUrl)
    }
}
