package server.token

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class TokenProvider(
    @Value("\${jwt.secret-key}") secretKey: String,
) {

    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun encodeToken(
        principal: AuthPrincipal,
        ttl: Long
    ): String {
        val now = Date()
        val expiry = Date(now.time + ttl)

        return Jwts.builder()
            .subject(principal.memberId.toString())
            .claim("type", principal.type.name)
            .apply {
                if (principal.type == TokenType.ACCESS) {
                    claim("role", principal.role)
                }
            }
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun decodeToken(token: String): AuthPrincipal =
        decodeClaims(token).let { payload ->
            AuthPrincipal(
                memberId = payload.subject.toLongOrNull().orInvalid(),
                role = payload.get("role", String::class.java),
                type = payload.get("type", String::class.java)
                    ?.let(TokenType::valueOf)
                    .orInvalid(),
            )
        }

    private fun decodeClaims(token: String) =
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: ExpiredJwtException) {
            throw ExpiredTokenException()
        } catch (_: JwtException) {
            throw InvalidTokenException()
        } catch (_: IllegalArgumentException) {
            throw InvalidTokenException()
        }

    private fun Long?.orInvalid(): Long = this ?: throw InvalidTokenException()

    private fun TokenType?.orInvalid(): TokenType = this ?: throw InvalidTokenException()
}
