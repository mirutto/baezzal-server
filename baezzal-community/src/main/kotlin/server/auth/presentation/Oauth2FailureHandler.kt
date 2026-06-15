package server.auth.presentation

import global.http.appendQueryParams
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import server.auth.application.Oauth2Service

@Component
class Oauth2FailureHandler(
    private val oauth2Service: Oauth2Service,
) : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException?,
    ) {
        val redirectUri = request.getParameter("state")
            ?.takeIf { it.isNotBlank() }
            ?.let(oauth2Service::takeRedirectUri)

        if (redirectUri == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "로그인 상태가 유효하지 않습니다")
            return
        }

        val redirectUrl = appendQueryParams(
            redirectUri,
            "type" to "hasError",
            "errorMessage" to (exception?.message ?: "로그인을 할 수 없습니다."),
        )
        response.status = HttpStatus.FOUND.value()
        response.sendRedirect(redirectUrl)
    }
}
