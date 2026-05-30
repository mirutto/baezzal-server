package server.team.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import server.team.domain.Team
import server.team.infrastructure.TeamCache
import server.team.infrastructure.TeamRepository

class TeamReaderTest {
    private val teamRepository = mockk<TeamRepository>()
    private val teamCache = mockk<TeamCache>()
    private val teamReader = TeamReader(teamRepository, teamCache)

    @Test
    fun `team id 로 단건 조회한다`() {
        val team = Team(id = 1L, name = "A", sortOrder = 1)
        every { teamRepository.findByIdOrNull(1L) } returns team

        val result = teamReader.readById(1L)

        result shouldBe team
        verify(exactly = 1) { teamRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `캐시가 있으면 DB 조회 없이 반환한다`() {
        val cachedTeams = listOf(Team(id = 1L, name = "A", sortOrder = 1))
        every { teamCache.getAll() } returns cachedTeams

        val result = teamReader.readAll()

        result shouldBe cachedTeams
        verify(exactly = 1) { teamCache.getAll() }
        verify(exactly = 0) { teamRepository.findAllByOrderBySortOrderAsc() }
    }

    @Test
    fun `캐시가 없으면 DB 조회 후 캐시에 저장한다`() {
        val teams = listOf(Team(id = 1L, name = "A", sortOrder = 1))
        every { teamCache.getAll() } returns null
        every { teamRepository.findAllByOrderBySortOrderAsc() } returns teams
        every { teamCache.setAll(teams) } just runs

        val result = teamReader.readAll()

        result shouldBe teams
        verify(exactly = 1) { teamCache.getAll() }
        verify(exactly = 1) { teamRepository.findAllByOrderBySortOrderAsc() }
        verify(exactly = 1) { teamCache.setAll(teams) }
    }
}
