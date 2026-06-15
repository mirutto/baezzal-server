package server.team.implementation

import global.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.team.domain.Team
import server.team.infrastructure.TeamCache
import server.team.infrastructure.TeamRepository
import java.util.concurrent.ConcurrentHashMap

@Component
class TeamReader(
    private val teamRepository: TeamRepository,
    private val teamCache: TeamCache,
) {
    private val secondaryCacheById = ConcurrentHashMap<Long, Team>()
    private val secondaryCacheByCode = ConcurrentHashMap<String, Team>()

    @Transactional(readOnly = true)
    fun readById(teamId: Long): Team = secondaryCacheById[teamId]
        ?: loadAllAndCache().firstOrNull { it.id == teamId }
        ?: throw NotFoundException("팀을 찾을 수 없습니다")

    @Transactional(readOnly = true)
    fun readByCode(teamCode: String): Team = secondaryCacheByCode[teamCode]
        ?: loadAllAndCache().firstOrNull { it.code == teamCode }
        ?: throw NotFoundException("팀을 찾을 수 없습니다")

    @Transactional(readOnly = true)
    fun resolveCode(teamId: Long?): String? = teamId?.let(::readById)?.code

    @Transactional(readOnly = true)
    fun readAll(): List<Team> = if (secondaryCacheById.isNotEmpty()) {
        secondaryCacheById.values.sortedBy(Team::sortOrder)
    } else {
        loadAllAndCache()
    }

    private fun loadAllAndCache(): List<Team> {
        val cachedTeams = teamCache.getAll()
        if (cachedTeams != null) {
            return cachedTeams.also(::cacheAll)
        }

        return teamRepository.findAllByOrderBySortOrderAsc()
            .also(teamCache::setAll)
            .also(::cacheAll)
    }

    private fun cacheAll(teams: List<Team>) {
        teams.forEach { team ->
            secondaryCacheById[team.id] = team
            secondaryCacheByCode[team.code] = team
        }
    }
}
