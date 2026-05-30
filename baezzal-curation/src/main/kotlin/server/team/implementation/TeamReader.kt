package server.team.implementation

import org.springframework.stereotype.Component
import server.team.domain.Team
import server.team.infrastructure.TeamCache
import server.team.infrastructure.TeamRepository

@Component
class TeamReader(
    private val teamRepository: TeamRepository,
    private val teamCache: TeamCache,
) {
    fun readAll(): List<Team> {
        val cachedTeams = teamCache.getAll()
        if (cachedTeams != null) {
            return cachedTeams
        }

        return teamRepository.findAllByOrderBySortOrderAsc().also(teamCache::setAll)
    }
}
