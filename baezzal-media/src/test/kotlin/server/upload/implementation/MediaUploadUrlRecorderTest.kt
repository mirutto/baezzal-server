package server.upload.implementation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory

class MediaUploadUrlRecorderTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val mediaUploadUrlRecorder = MediaUploadUrlRecorder(cacheMemory)

    @Test
    fun `post image url 을 검증용 캐시에 기록한다`() {
        every {
            cacheMemory.set(
                key = "post:image-url:https://static.wowan.me/posts/1.png",
                value = true,
                ttlMillis = 600_000L,
            )
        } returns Unit

        mediaUploadUrlRecorder.recordIssued(
            prefix = "posts",
            fileUrl = "https://static.wowan.me/posts/1.png",
            expiresInSeconds = 600,
        )

        verify(exactly = 1) {
            cacheMemory.set(
                key = "post:image-url:https://static.wowan.me/posts/1.png",
                value = true,
                ttlMillis = 600_000L,
            )
        }
    }

    @Test
    fun `profile image url 을 검증용 캐시에 기록한다`() {
        every {
            cacheMemory.set(
                key = "member:profile-image-url:https://static.wowan.me/profiles/1.png",
                value = true,
                ttlMillis = 300_000L,
            )
        } returns Unit

        mediaUploadUrlRecorder.recordIssued(
            prefix = "profiles",
            fileUrl = "https://static.wowan.me/profiles/1.png",
            expiresInSeconds = 300,
        )

        verify(exactly = 1) {
            cacheMemory.set(
                key = "member:profile-image-url:https://static.wowan.me/profiles/1.png",
                value = true,
                ttlMillis = 300_000L,
            )
        }
    }

    @Test
    fun `지원하지 않는 prefix 는 기록하지 않는다`() {
        mediaUploadUrlRecorder.recordIssued(
            prefix = "banners",
            fileUrl = "https://static.wowan.me/banners/1.png",
            expiresInSeconds = 300,
        )

        verify(exactly = 0) { cacheMemory.set(any(), any<Boolean>(), any()) }
    }
}
