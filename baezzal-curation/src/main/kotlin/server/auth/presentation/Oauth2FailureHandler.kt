package server.auth.presentation

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class Oauth2FailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException?,
    ) {
        val redirectUrl = request.resolveOauth2RedirectUrl(
            "type" to "hasError",
            "errorMessage" to (exception?.message ?: "로그인을 할 수 없습니다."),
        )
        response.expireOauth2RedirectUriCookie(secure = request.isSecure)
        response.status = HttpStatus.FOUND.value()
        response.sendRedirect(redirectUrl)
    }
}
