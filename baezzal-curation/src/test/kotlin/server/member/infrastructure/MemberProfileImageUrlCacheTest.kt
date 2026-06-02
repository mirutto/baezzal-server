package server.member.infrastructure

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory

class MemberProfileImageUrlCacheTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val memberProfileImageUrlCache = MemberProfileImageUrlCache(cacheMemory)

    @Test
    fun `발급된 profile image url 을 조회한다`() {
        every {
            cacheMemory.get(
                key = "member:profile-image-url:https://static.wowan.me/baezzal-images/profiles/123",
                type = Boolean::class.java,
            )
        } returns true

        val result = memberProfileImageUrlCache.isIssued("https://static.wowan.me/baezzal-images/profiles/123")

        result shouldBe true
    }

    @Test
    fun `발급된 profile image url 을 ttl 과 함께 저장한다`() {
        every {
            cacheMemory.set(
                key = "member:profile-image-url:https://static.wowan.me/baezzal-images/profiles/123",
                value = true,
                ttlMillis = 600_000L,
            )
        } returns Unit

        memberProfileImageUrlCache.setIssued(
            imageUrl = "https://static.wowan.me/baezzal-images/profiles/123",
            ttlMillis = 600_000L,
        )

        verify(exactly = 1) {
            cacheMemory.set(
                key = "member:profile-image-url:https://static.wowan.me/baezzal-images/profiles/123",
                value = true,
                ttlMillis = 600_000L,
            )
        }
    }
}
