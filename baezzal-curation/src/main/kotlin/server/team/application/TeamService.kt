package server.team.application

import org.springframework.stereotype.Service
import server.team.implementation.TeamReader

@Service
class TeamService(
    private val teamReader: TeamReader,
) {
    fun findAll(): List<TeamData> = teamReader.readAll().map(::TeamData)
}
