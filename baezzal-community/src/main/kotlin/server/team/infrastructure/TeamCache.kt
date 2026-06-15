package server.team.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.cache.get
import server.team.domain.Team

@Component
class TeamCache(
    private val cacheMemory: CacheMemory,
) {
    fun getAll(): List<Team>? = cacheMemory.get<List<Team>>(ALL_TEAMS_KEY)

    fun setAll(teams: List<Team>) {
        cacheMemory.set(ALL_TEAMS_KEY, teams, null)
    }

    companion object {
        private const val ALL_TEAMS_KEY = "team:all"
    }
}
