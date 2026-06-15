package server.team.application

import server.team.domain.Team

data class TeamData(
    val code: String,
    val name: String,
) {
    constructor(team: Team) : this(
        code = team.code,
        name = team.name,
    )
}
