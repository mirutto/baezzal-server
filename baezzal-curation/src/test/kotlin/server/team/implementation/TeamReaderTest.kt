package server.team.implementation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import global.error.NotFoundException
import org.junit.jupiter.api.Test
import server.team.domain.Team
import server.team.infrastructure.TeamCache
import server.team.infrastructure.TeamRepository

class TeamReaderTest {
    private val teamRepository = mockk<TeamRepository>()
    private val teamCache = mockk<TeamCache>()
    private val teamReader = TeamReader(teamRepository, teamCache)

    @Test
    fun `team id 로 단건 조회한다`() {
        val team = Team(id = 1L, code = "LG", name = "A", sortOrder = 1)
        every { teamCache.getAll() } returns listOf(team)

        val result = teamReader.readById(1L)

        result shouldBe team
        verify(exactly = 1) { teamCache.getAll() }
    }

    @Test
    fun `캐시가 있으면 DB 조회 없이 반환한다`() {
        val cachedTeams = listOf(Team(id = 1L, code = "LG", name = "A", sortOrder = 1))
        every { teamCache.getAll() } returns cachedTeams

        val result = teamReader.readAll()

        result shouldBe cachedTeams
        verify(exactly = 1) { teamCache.getAll() }
        verify(exactly = 0) { teamRepository.findAllByOrderBySortOrderAsc() }
    }

    @Test
    fun `캐시가 없으면 DB 조회 후 캐시에 저장한다`() {
        val teams = listOf(Team(id = 1L, code = "LG", name = "A", sortOrder = 1))
        every { teamCache.getAll() } returns null
        every { teamRepository.findAllByOrderBySortOrderAsc() } returns teams
        every { teamCache.setAll(teams) } just runs

        val result = teamReader.readAll()

        result shouldBe teams
        verify(exactly = 1) { teamCache.getAll() }
        verify(exactly = 1) { teamRepository.findAllByOrderBySortOrderAsc() }
        verify(exactly = 1) { teamCache.setAll(teams) }
    }

    @Test
    fun `team code 로 단건 조회한다`() {
        val team = Team(id = 1L, code = "LG", name = "A", sortOrder = 1)
        every { teamCache.getAll() } returns listOf(team)

        val result = teamReader.readByCode("LG")

        result shouldBe team
    }

    @Test
    fun `2차 캐시에 있으면 재조회하지 않는다`() {
        val team = Team(id = 1L, code = "LG", name = "A", sortOrder = 1)
        every { teamCache.getAll() } returns listOf(team)

        teamReader.readById(1L)
        teamReader.readByCode("LG")

        verify(exactly = 1) { teamCache.getAll() }
        verify(exactly = 0) { teamRepository.findAllByOrderBySortOrderAsc() }
    }

    @Test
    fun `team code 로 필수 조회한다`() {
        val team = Team(id = 1L, code = "LG", name = "A", sortOrder = 1)
        every { teamCache.getAll() } returns listOf(team)

        val result = teamReader.readByCode("LG")

        result shouldBe team
    }

    @Test
    fun `존재하지 않는 team code 필수 조회는 예외가 발생한다`() {
        every { teamCache.getAll() } returns emptyList()

        shouldThrow<NotFoundException> {
            teamReader.readByCode("LG")
        }
    }
}
