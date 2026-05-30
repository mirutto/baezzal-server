package server.auth.presentation

import global.web.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.auth.application.AuthService
import server.auth.application.AuthTicketExchangeRequest
import server.auth.application.AuthTokenData

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @GetMapping("/oauth2/{provider}")
    fun startOauth2Login(
        @PathVariable provider: String,
        @RequestParam("redirect_uri") redirectUri: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        redirectUri
            .let { response.appendOauth2RedirectUriCookie(it, secure = request.isSecure) }

        response.status = HttpStatus.FOUND.value()
        response.sendRedirect("/oauth2/authorization/$provider")
    }

    @PostMapping("/ticket/exchange")
    fun exchangeTicket(
        @RequestBody request: AuthTicketExchangeRequest,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<AuthTokenData> {
        val authToken = authService.exchangeTicket(request.ticket)
        response.appendAuthCookies(
            accessToken = authToken.accessToken,
            refreshToken = authToken.refreshToken,
            secure = httpRequest.isSecure,
        )
        return ApiResponse.of(authToken)
    }

    @PostMapping("/token/reissue")
    fun reissueToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<AuthTokenData> {
        val authToken = authService.reissue(request.requireRefreshToken())
        response.appendAuthCookies(
            accessToken = authToken.accessToken,
            refreshToken = authToken.refreshToken,
            secure = request.isSecure,
        )
        return ApiResponse.of(authToken)
    }
}
