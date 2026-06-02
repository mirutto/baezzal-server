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
import server.token.AuthPrincipal
import server.token.InvalidTokenException
import server.token.TokenProvider
import server.token.TokenType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
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
    ): Any? =
        when (val result = resolvePassport(webRequest)) {
            PassportResolutionResult.Invalid ->
                nullableResultOrThrow(parameter.isNullablePassport(), "LOGIN_AGAIN")
            PassportResolutionResult.Expired ->
                nullableResultOrThrow(parameter.isNullablePassport(), "TOKEN_EXPIRED")
            is PassportResolutionResult.Success -> result.passport
        }

    private fun resolvePassport(webRequest: NativeWebRequest): PassportResolutionResult =
        webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?.resolveAccessToken()
            ?.let(::decodePassport)
            ?: PassportResolutionResult.Invalid

    private fun decodePassport(authorization: String): PassportResolutionResult =
        try {
            tokenProvider.decodeToken(authorization).toPassportResolutionResult()
        } catch (_: InvalidTokenException) {
            PassportResolutionResult.Invalid
        } catch (_: ExpiredTokenException) {
            PassportResolutionResult.Expired
        }

    private fun AuthPrincipal.toPassportResolutionResult(): PassportResolutionResult {
        val role = role

        return if (type == TokenType.ACCESS && role != null) {
            PassportResolutionResult.Success(
                Passport(
                    memberId = memberId,
                    role = role,
                ),
            )
        } else {
            PassportResolutionResult.Invalid
        }
    }

    private fun nullableResultOrThrow(isNullablePassport: Boolean, message: String): Nothing? =
        if (isNullablePassport) {
            null
        } else {
            throw UnauthorizedException(message)
        }

    private fun MethodParameter.isNullablePassport(): Boolean =
        executable.toKotlinFunction()
            ?.valueParameters
            ?.getOrNull(parameterIndex)
            ?.type
            ?.isMarkedNullable == true

    private fun java.lang.reflect.Executable.toKotlinFunction(): KFunction<*>? =
        when (this) {
            is java.lang.reflect.Method -> kotlinFunction
            is java.lang.reflect.Constructor<*> -> null
        }

    private val KFunction<*>.valueParameters: List<KParameter>
        get() = parameters.filter { it.kind == KParameter.Kind.VALUE }

    private fun HttpServletRequest.resolveAccessToken(): String? =
        getHeader(AUTHORIZATION_HEADER)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.takeIf { it.isNotBlank() }

    private sealed interface PassportResolutionResult {
        data object Invalid : PassportResolutionResult

        data object Expired : PassportResolutionResult

        data class Success(
            val passport: Passport,
        ) : PassportResolutionResult
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
