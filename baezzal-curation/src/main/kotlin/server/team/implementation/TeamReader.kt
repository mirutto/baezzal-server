package server.team.implementation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.team.domain.Team
import server.team.infrastructure.TeamCache
import server.team.infrastructure.TeamRepository

@Component
class TeamReader(
    private val teamRepository: TeamRepository,
    private val teamCache: TeamCache,
) {
    @Transactional(readOnly = true)
    fun readById(teamId: Long): Team? = teamRepository.findByIdOrNull(teamId)

    @Transactional(readOnly = true)
    fun readAll(): List<Team> {
        val cachedTeams = teamCache.getAll()
        if (cachedTeams != null) {
            return cachedTeams
        }

        return teamRepository.findAllByOrderBySortOrderAsc().also(teamCache::setAll)
    }
}
