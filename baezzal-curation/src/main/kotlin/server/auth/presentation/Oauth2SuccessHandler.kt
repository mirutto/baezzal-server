package server.auth.presentation

import global.http.appendQueryParams
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import server.auth.application.AuthService
import server.auth.application.Oauth2Service

@Component
class Oauth2SuccessHandler(
    private val authService: AuthService,
    private val oauth2Service: Oauth2Service,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val redirectUri = request.getParameter("state")
            ?.takeIf { it.isNotBlank() }
            ?.let(oauth2Service::takeRedirectUri)

        if (redirectUri == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "로그인 상태가 유효하지 않습니다")
            return
        }

        val authenticatedUser = authentication.principal as Oauth2SocialUser

        val redirectUrl = when (authenticatedUser) {
            is Oauth2SocialUser.Authenticated -> {
                val ticket = authService.issueLoginTicket(
                    memberId = authenticatedUser.memberId,
                    role = authenticatedUser.role,
                )

                appendQueryParams(
                    redirectUri,
                    "type" to "success",
                    "ticket" to ticket,
                )
            }

            is Oauth2SocialUser.HasError -> {
                appendQueryParams(
                    redirectUri,
                    "type" to "hasError",
                    "errorMessage" to authenticatedUser.message,
                )
            }
        }

        response.status = HttpStatus.FOUND.value()
        response.sendRedirect(redirectUrl)
    }
}
