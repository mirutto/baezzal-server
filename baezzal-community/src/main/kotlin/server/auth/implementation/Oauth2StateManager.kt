package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.infrastructure.Oauth2StateCache
import java.util.UUID

@Component
class Oauth2StateManager(
    private val oauth2StateCache: Oauth2StateCache,
) {
    fun issue(redirectUri: String): String {
        val state = UUID.randomUUID().toString()
        oauth2StateCache.save(state = state, redirectUri = redirectUri)
        return state
    }

    fun take(state: String): String? = oauth2StateCache.take(state)
}
