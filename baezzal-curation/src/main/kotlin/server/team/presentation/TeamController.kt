package server.team.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.team.application.TeamData
import server.team.application.TeamService

@RestController
@RequestMapping("/api/v1/team")
class TeamController(
    private val teamService: TeamService,
) {
    @GetMapping
    fun findAll(): List<TeamData> = teamService.findAll()
}
