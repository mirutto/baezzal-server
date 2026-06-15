package server.auth.presentation

import global.web.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.auth.application.AuthService
import server.auth.application.AuthLogoutCommand
import server.auth.application.AuthTicketExchangeCommand
import server.auth.application.AuthTicketExchangeResult
import server.auth.application.AuthTokenResult
import server.auth.application.AuthTokenReissueCommand
import server.auth.application.Oauth2LoginStartCommand
import server.auth.application.Oauth2Service

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val oauth2Service: Oauth2Service,
) {
    @GetMapping("/oauth2/{provider}")
    fun startOauth2Login(
        @PathVariable provider: String,
        @RequestParam("redirect_uri") redirectUri: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val authorizationUri = oauth2Service.start(
            Oauth2LoginStartCommand(
                provider = provider,
                redirectUri = redirectUri,
                baseScheme = request.scheme,
                baseHost = request.serverName,
                basePort = request.serverPort,
                basePath = request.contextPath,
            ),
        )

        response.status = HttpStatus.FOUND.value()
        response.sendRedirect(authorizationUri)
    }

    @PostMapping("/ticket/exchange")
    fun exchangeTicket(
        @RequestBody command: AuthTicketExchangeCommand,
    ): ApiResponse<AuthTicketExchangeResult> {
        val authToken = authService.exchangeTicket(command.ticket)
        return ApiResponse.of(authToken)
    }

    @PostMapping("/token/reissue")
    fun reissueToken(
        @RequestBody command: AuthTokenReissueCommand,
    ): ApiResponse<AuthTokenResult> {
        val authToken = authService.reissue(command.refreshToken)
        return ApiResponse.of(authToken)
    }

    @DeleteMapping("/logout")
    fun logout(
        @RequestBody command: AuthLogoutCommand,
    ): ApiResponse<Unit> {
        authService.logout(command.refreshToken)
        return ApiResponse.of(status = HttpStatus.NO_CONTENT)
    }
}
