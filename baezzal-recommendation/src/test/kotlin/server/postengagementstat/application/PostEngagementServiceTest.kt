package server.postengagementstat.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.postengagementstat.implementation.PostEngagementStatDailyWriter
import java.time.LocalDate
import java.time.LocalDateTime

class PostEngagementServiceTest {
    private val postEngagementStatDailyWriter = mockk<PostEngagementStatDailyWriter>(relaxed = true)
    private val postEngagementService = PostEngagementService(
        postEngagementStatDailyWriter = postEngagementStatDailyWriter,
    )

    @Test
    fun `post 조회 시 view count 를 누적한다`() {
        val viewedAt = LocalDateTime.of(2026, 6, 29, 10, 30, 0)

        postEngagementService.recordPostView(
            PostViewedEvent(
                userId = 7L,
                postId = 100L,
                viewedAt = viewedAt,
            ),
        )

        verify(exactly = 1) {
            postEngagementStatDailyWriter.incrementViewCount(
                postId = 100L,
                statDate = LocalDate.of(2026, 6, 29),
            )
        }
    }

    @Test
    fun `컬렉션 추가 시 collection added count 를 누적한다`() {
        val addedAt = LocalDateTime.of(2026, 6, 29, 11, 0, 0)

        postEngagementService.recordCollectionPostAdded(
            CollectionPostAddedEvent(
                userId = 7L,
                collectionId = 1L,
                postId = 100L,
                addedAt = addedAt,
            ),
        )

        verify(exactly = 1) {
            postEngagementStatDailyWriter.incrementCollectionAddedCount(
                postId = 100L,
                statDate = LocalDate.of(2026, 6, 29),
            )
        }
    }
}
