package server.post.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.post.implementation.PostBatchLocker
import server.post.implementation.PostViewCountBatchReader
import server.post.implementation.PostWriter

class PostBatchServiceTest {
    private val postBatchLocker = mockk<PostBatchLocker>()
    private val postWriter = mockk<PostWriter>()
    private val postViewCountBatchReader = mockk<PostViewCountBatchReader>()
    private val postBatchService = PostBatchService(
        postBatchLocker = postBatchLocker,
        postWriter = postWriter,
        postViewCountBatchReader = postViewCountBatchReader,
    )

    @Test
    fun `누적된 조회수를 post 에 반영한다`() {
        every { postBatchLocker.withLock(any<() -> PostBatchResult>()) } answers {
            firstArg<() -> PostBatchResult>().invoke()
        }
        every { postViewCountBatchReader.readPendingViewCounts() } returns mapOf(
            100L to 3L,
            102L to 5L,
        )
        every {
            postWriter.increaseViewCounts(
                mapOf(
                    100L to 3L,
                    102L to 5L,
                ),
            )
        } returns Unit
        every { postViewCountBatchReader.decreaseViewCount(100L, 3L) } returns 0L
        every { postViewCountBatchReader.decreaseViewCount(102L, 5L) } returns 1L

        val result = postBatchService.updateViewCounts()

        result shouldBe PostBatchResult(
            postCount = 2,
            viewCount = 8L,
        )
        verify(exactly = 1) {
            postWriter.increaseViewCounts(
                mapOf(
                    100L to 3L,
                    102L to 5L,
                ),
            )
        }
        verify(exactly = 1) { postViewCountBatchReader.decreaseViewCount(100L, 3L) }
        verify(exactly = 1) { postViewCountBatchReader.decreaseViewCount(102L, 5L) }
    }

    @Test
    fun `반영할 조회수가 없으면 비어있는 결과를 반환한다`() {
        every { postBatchLocker.withLock(any<() -> PostBatchResult>()) } answers {
            firstArg<() -> PostBatchResult>().invoke()
        }
        every { postViewCountBatchReader.readPendingViewCounts() } returns emptyMap()

        val result = postBatchService.updateViewCounts()

        result shouldBe PostBatchResult(
            postCount = 0,
            viewCount = 0L,
        )
        verify(exactly = 0) { postWriter.increaseViewCounts(any()) }
        verify(exactly = 0) { postViewCountBatchReader.decreaseViewCount(any(), any()) }
    }
}
