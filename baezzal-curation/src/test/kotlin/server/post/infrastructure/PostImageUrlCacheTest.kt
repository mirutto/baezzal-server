package server.post.infrastructure

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory

class PostImageUrlCacheTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val postImageUrlCache = PostImageUrlCache(cacheMemory)

    @Test
    fun `발급된 image url 을 조회한다`() {
        every {
            cacheMemory.get(
                key = "post:image-url:https://static.wowan.me/baezzal-images/posts/123",
                type = Boolean::class.java,
            )
        } returns true

        val result = postImageUrlCache.isIssued("https://static.wowan.me/baezzal-images/posts/123")

        result shouldBe true
    }

    @Test
    fun `발급된 image url 을 ttl 과 함께 저장한다`() {
        every {
            cacheMemory.set(
                key = "post:image-url:https://static.wowan.me/baezzal-images/posts/123",
                value = true,
                ttlMillis = 600_000L,
            )
        } returns Unit

        postImageUrlCache.setIssued(
            imageUrl = "https://static.wowan.me/baezzal-images/posts/123",
            ttlMillis = 600_000L,
        )

        verify(exactly = 1) {
            cacheMemory.set(
                key = "post:image-url:https://static.wowan.me/baezzal-images/posts/123",
                value = true,
                ttlMillis = 600_000L,
            )
        }
    }
}
