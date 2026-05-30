package server.team.application

import server.team.domain.Team

data class TeamData(
    val teamId: Long,
    val name: String,
) {
    constructor(team: Team) : this(
        teamId = team.id,
        name = team.name
    )
}
