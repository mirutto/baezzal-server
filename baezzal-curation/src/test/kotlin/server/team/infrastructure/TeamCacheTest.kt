package server.team.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory
import server.team.domain.Team

class TeamCacheTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val teamCache = TeamCache(cacheMemory)

    @Test
    fun `getAll 은 team 전체 캐시 키로 조회한다`() {
        val teams = listOf(Team(id = 1L, code = "LG", name = "A", sortOrder = 1))
        every {
            cacheMemory.get("team:all", any<TypeReference<List<Team>>>())
        } returns teams

        val result = teamCache.getAll()

        result shouldBe teams
        verify(exactly = 1) {
            cacheMemory.get("team:all", any<TypeReference<List<Team>>>())
        }
    }

    @Test
    fun `setAll 은 team 전체 목록을 ttl 없이 저장한다`() {
        val teams = listOf(Team(id = 1L, code = "LG", name = "A", sortOrder = 1))
        every { cacheMemory.set("team:all", teams, null) } returns Unit

        teamCache.setAll(teams)

        verify(exactly = 1) {
            cacheMemory.set("team:all", teams, null)
        }
    }
}
