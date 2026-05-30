package server.team.infrastructure

import org.springframework.stereotype.Component
import server.cache.RedisCache
import server.cache.get
import server.team.domain.Team

@Component
class TeamCache(
    private val redisCache: RedisCache,
) {
    fun getAll(): List<Team>? = redisCache.get<List<Team>>(ALL_TEAMS_KEY)

    fun setAll(teams: List<Team>) {
        redisCache.set(ALL_TEAMS_KEY, teams, null)
    }

    companion object {
        private const val ALL_TEAMS_KEY = "team:all"
    }
}
