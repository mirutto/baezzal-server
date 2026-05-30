package server.team.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.team.domain.Team

interface TeamRepository : JpaRepository<Team, Long> {
    fun findAllByOrderBySortOrderAsc(): List<Team>
}
