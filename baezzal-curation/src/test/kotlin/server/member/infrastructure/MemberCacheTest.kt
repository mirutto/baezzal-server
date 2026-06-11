package server.member.infrastructure

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole

class MemberCacheTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val memberCache = MemberCache(cacheMemory)

    @Test
    fun `id 로 member 캐시를 조회한다`() {
        every {
            cacheMemory.get(
                key = "member:id:1",
                type = MemberCache.MemberCacheData::class.java,
            )
        } returns MemberCache.MemberCacheData(member(username = "cache-user"))

        val result = memberCache.getById(1L)

        result shouldBe member(username = "cache-user")
    }

    @Test
    fun `username 으로 member 캐시를 조회한다`() {
        every {
            cacheMemory.get(
                key = "member:username:cache-user",
                type = MemberCache.MemberCacheData::class.java,
            )
        } returns MemberCache.MemberCacheData(member(username = "cache-user"))

        val result = memberCache.getByUsername("cache-user")

        result shouldBe member(username = "cache-user")
    }

    @Test
    fun `member 를 id 와 username 키로 함께 저장한다`() {
        val member = member(username = "cache-user")
        every { cacheMemory.set("member:id:1", any<Any>(), null) } returns Unit
        every { cacheMemory.set("member:username:cache-user", any<Any>(), null) } returns Unit

        memberCache.set(member)

        verify(exactly = 1) { cacheMemory.set("member:id:1", any<Any>(), null) }
        verify(exactly = 1) { cacheMemory.set("member:username:cache-user", any<Any>(), null) }
    }

    @Test
    fun `member 캐시를 id 와 username 키로 함께 삭제한다`() {
        every { cacheMemory.evict("member:id:1") } returns true
        every { cacheMemory.evict("member:username:cache-user") } returns true

        memberCache.evict(memberId = 1L, username = "cache-user")

        verify(exactly = 1) { cacheMemory.evict("member:id:1") }
        verify(exactly = 1) { cacheMemory.evict("member:username:cache-user") }
    }

    private fun member(username: String): Member = Member(
        id = 1L,
        nickname = "cache-nickname",
        username = username,
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key",
        profileImage = "https://example.com/profile.png",
        description = "cache-description",
        preferredTeamId = 3L,
        role = MemberRole.USER,
    )
}
