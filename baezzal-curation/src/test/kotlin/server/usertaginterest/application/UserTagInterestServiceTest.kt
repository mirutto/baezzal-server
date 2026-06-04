package server.usertaginterest.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.collection.application.CollectionPostAddedEvent
import server.post.application.PostViewedEvent
import server.posttag.domain.PostTag
import server.posttag.implementation.PostTagReader
import server.usertaginterest.domain.UserTagInterest
import server.usertaginterest.implementation.UserTagInterestReader
import server.usertaginterest.implementation.UserTagInterestWriter
import java.time.LocalDateTime

class UserTagInterestServiceTest {
    private val postTagReader = mockk<PostTagReader>()
    private val userTagInterestReader = mockk<UserTagInterestReader>()
    private val userTagInterestWriter = mockk<UserTagInterestWriter>()
    private val userTagInterestService = UserTagInterestService(
        postTagReader = postTagReader,
        userTagInterestReader = userTagInterestReader,
        userTagInterestWriter = userTagInterestWriter,
    )

    @Test
    fun `post 조회 시 태그 관심도를 생성한다`() {
        val capturedInterests = slot<Collection<UserTagInterest>>()
        val viewedAt = LocalDateTime.of(2026, 6, 3, 10, 30, 0)

        every { postTagReader.readAllByPostId(100L) } returns listOf(
            PostTag(id = 1L, postId = 100L, tagId = 10L),
            PostTag(id = 2L, postId = 100L, tagId = 20L),
        )
        every { userTagInterestReader.readAllByUserIdAndTagIds(7L, listOf(10L, 20L)) } returns emptyList()
        every {
            userTagInterestWriter.writeAll(capture(capturedInterests))
        } answers {
            firstArg<List<UserTagInterest>>()
        }

        userTagInterestService.recordPostView(
            PostViewedEvent(
                userId = 7L,
                postId = 100L,
                viewedAt = viewedAt,
            ),
        )

        capturedInterests.captured.map { it.userId } shouldBe listOf(7L, 7L)
        capturedInterests.captured.map { it.tagId } shouldBe listOf(10L, 20L)
        capturedInterests.captured.map { it.score } shouldBe listOf(2, 2)
        capturedInterests.captured.map { it.lastInteractedAt } shouldBe listOf(viewedAt, viewedAt)
    }

    @Test
    fun `컬렉션 추가 시 기존 태그 관심도를 누적한다`() {
        val addedAt = LocalDateTime.of(2026, 6, 3, 11, 0, 0)
        val existing = UserTagInterest(
            id = 3L,
            userId = 7L,
            tagId = 10L,
            score = 2,
            lastInteractedAt = LocalDateTime.of(2026, 6, 2, 9, 0, 0),
        )

        every { postTagReader.readAllByPostId(100L) } returns listOf(
            PostTag(id = 1L, postId = 100L, tagId = 10L),
            PostTag(id = 2L, postId = 100L, tagId = 20L),
        )
        every { userTagInterestReader.readAllByUserIdAndTagIds(7L, listOf(10L, 20L)) } returns listOf(existing)
        every { userTagInterestWriter.writeAll(any()) } answers { firstArg<List<UserTagInterest>>() }

        userTagInterestService.recordCollectionPostAdded(
            CollectionPostAddedEvent(
                userId = 7L,
                collectionId = 1L,
                postId = 100L,
                addedAt = addedAt,
            ),
        )

        existing.score shouldBe 7
        existing.lastInteractedAt shouldBe addedAt
        verify(exactly = 1) { userTagInterestWriter.writeAll(any()) }
    }
}
