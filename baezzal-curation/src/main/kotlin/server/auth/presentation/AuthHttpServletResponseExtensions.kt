package server.auth.presentation

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val OAUTH2_REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
private const val COOKIE_MAX_AGE = 7 * 24 * 60 * 60L

fun HttpServletResponse.appendOauth2RedirectUriCookie(
    redirectUri: String,
    secure: Boolean,
) {
    addHeader(
        HttpHeaders.SET_COOKIE,
        createCookie(
            name = OAUTH2_REDIRECT_URI_COOKIE,
            value = encode(redirectUri),
            maxAge = COOKIE_MAX_AGE,
            secure = secure,
        ).toString(),
    )
}

fun HttpServletResponse.expireOauth2RedirectUriCookie(
    secure: Boolean,
) {
    addHeader(
        HttpHeaders.SET_COOKIE,
        createCookie(
            name = OAUTH2_REDIRECT_URI_COOKIE,
            value = "",
            maxAge = 0,
            secure = secure,
        ).toString(),
    )
}

private fun createCookie(
    name: String,
    value: String,
    maxAge: Long,
    secure: Boolean,
): ResponseCookie =
    ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(if (secure) "None" else "Lax")
        .path("/")
        .maxAge(maxAge)
        .build()

private fun encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8)
