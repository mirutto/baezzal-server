package global.auth

import global.error.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import server.token.ExpiredTokenException
import server.token.InvalidTokenException
import server.token.TokenProvider
import server.token.TokenType

@Component
class PassportResolver(
    private val tokenProvider: TokenProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(RequestPassport::class.java) &&
            parameter.parameterType == Passport::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val authorization = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?.getHeader(AUTHORIZATION_HEADER)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.takeIf { it.isNotBlank() }
            ?: invalidToken()
        val principal =
            try {
                tokenProvider.decodeToken(authorization)
            } catch (_: InvalidTokenException) {
                invalidToken()
            } catch (_: ExpiredTokenException) {
                expiredToken()
            }.takeIf { it.type == TokenType.ACCESS && it.role != null }
                ?: invalidToken()
        val role = principal.role ?: invalidToken()

        return Passport(
            memberId = principal.memberId,
            role = role,
        )
    }

    private fun invalidToken(): Nothing = throw UnauthorizedException("LOGIN_AGAIN")

    private fun expiredToken(): Nothing = throw UnauthorizedException("TOKEN_EXPIRED")

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
