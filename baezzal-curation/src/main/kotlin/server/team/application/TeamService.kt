package server.team.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.team.implementation.TeamReader

@Service
class TeamService(
    private val teamReader: TeamReader,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<TeamData> = teamReader.readAll().map(::TeamData)
}
