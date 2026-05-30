package server.auth.presentation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.env.Environment
import server.token.InvalidTokenException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val OAUTH2_REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
private const val REFRESH_TOKEN_COOKIE = "refresh_token"

fun HttpServletRequest.oauth2RedirectUri(): String? =
    cookies
        ?.firstOrNull { it.name == OAUTH2_REDIRECT_URI_COOKIE }
        ?.value
        ?.takeIf { it.isNotBlank() }
        ?.let(::decode)

fun HttpServletRequest.requireRefreshToken(): String =
    cookies
        ?.firstOrNull { it.name == REFRESH_TOKEN_COOKIE }
        ?.value
        ?.takeIf { it.isNotBlank() }
        ?: throw InvalidTokenException()

fun HttpServletRequest.resolveOauth2RedirectUrl(
    vararg param: Pair<String, String>,
): String {
    val queryParams = param.joinToString("&") { (key, value) ->
        "${encode(key)}=${encode(value)}"
    }
    val baseUrl = oauth2RedirectUri()
    return "$baseUrl?$queryParams"
}

private fun encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8)

private fun decode(value: String): String =
    URLDecoder.decode(value, StandardCharsets.UTF_8)
