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
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

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
    ): Any? {
        val isNullable = parameter.isNullablePassport()

        fun unauthorized(message: String = "LOGIN_AGAIN"): Any? =
            if (isNullable) {
                null
            } else {
                throw UnauthorizedException(message)
            }

        val result = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?.let(::resolvePassport)
            ?: PassportResolution.Unauthorized()

        return when (result) {
            is PassportResolution.Success -> result.passport
            is PassportResolution.Unauthorized -> unauthorized(result.message)
        }
    }

    private fun resolvePassport(request: HttpServletRequest): PassportResolution {
        val accessToken = request.resolveAccessToken()
            ?: return PassportResolution.Unauthorized()

        return try {
            val principal = tokenProvider.decodeToken(accessToken)
            val role = principal.role

            if (principal.type == TokenType.ACCESS && role != null) {
                PassportResolution.Success(
                    Passport(
                        memberId = principal.memberId,
                        role = role,
                    ),
                )
            } else {
                PassportResolution.Unauthorized()
            }
        } catch (_: InvalidTokenException) {
            PassportResolution.Unauthorized()
        } catch (_: ExpiredTokenException) {
            PassportResolution.Unauthorized("TOKEN_EXPIRED")
        }
    }

    private fun MethodParameter.isNullablePassport(): Boolean =
        method
            ?.kotlinFunction
            ?.valueParameters
            ?.getOrNull(parameterIndex)
            ?.type
            ?.isMarkedNullable == true

    private fun HttpServletRequest.resolveAccessToken(): String? =
        getHeader(AUTHORIZATION_HEADER)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.takeIf { it.isNotBlank() }

    private sealed interface PassportResolution {
        data class Success(
            val passport: Passport,
        ) : PassportResolution

        data class Unauthorized(
            val message: String = "LOGIN_AGAIN",
        ) : PassportResolution
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
