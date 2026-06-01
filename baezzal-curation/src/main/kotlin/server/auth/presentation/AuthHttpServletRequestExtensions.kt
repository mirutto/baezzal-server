package server.auth.presentation

import global.error.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val OAUTH2_REDIRECT_URI_COOKIE = "oauth2_redirect_uri"

fun HttpServletRequest.oauth2RedirectUri(): String? =
    cookies
        ?.firstOrNull { it.name == OAUTH2_REDIRECT_URI_COOKIE }
        ?.value
        ?.takeIf { it.isNotBlank() }
        ?.let(::decode)

fun HttpServletRequest.resolveOauth2RedirectUrl(
    vararg param: Pair<String, String>,
): String {
    val queryParams = param.joinToString("&") { (key, value) ->
        "${encode(key)}=${encode(value)}"
    }
    val baseUrl = oauth2RedirectUri()
        ?: throw UnauthorizedException("로그인 리다이렉트 경로가 없습니다")
    return "$baseUrl?$queryParams"
}

private fun encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8)

private fun decode(value: String): String =
    URLDecoder.decode(value, StandardCharsets.UTF_8)
