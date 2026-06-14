package server.feed.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.feed.implementation.FeedCollectionReader
import server.feed.implementation.FeedPostViewEventPublisher
import server.feed.implementation.FeedPostViewRecorder
import server.feed.implementation.FeedReader
import server.feed.implementation.FeedTeamReader
import java.time.LocalDateTime

class FeedServiceTest {
    private val feedReader = mockk<FeedReader>()
    private val feedCollectionReader = mockk<FeedCollectionReader>()
    private val feedTeamReader = mockk<FeedTeamReader>()
    private val feedPostViewRecorder = mockk<FeedPostViewRecorder>()
    private val feedPostViewEventPublisher = mockk<FeedPostViewEventPublisher>()
    private val feedService = FeedService(
        feedReader = feedReader,
        feedCollectionReader = feedCollectionReader,
        feedTeamReader = feedTeamReader,
        feedPostViewRecorder = feedPostViewRecorder,
        feedPostViewEventPublisher = feedPostViewEventPublisher,
    )

    @Test
    fun `내 collection 목록을 조회한다`() {
        val expected = listOf(
            FeedCollectionData(
                collectionId = 1L,
                name = "직관 모음",
                postCount = 3L,
                lastPostRuleModifiedAt = LocalDateTime.of(2026, 6, 13, 10, 0, 0),
                thumbnailUrl = "https://cdn.example.com/collections/1-thumb.webp",
                isPublic = false,
            ),
        )
        every { feedCollectionReader.readAllByMemberId(7L) } returns expected

        val result = feedService.findMyCollections(7L)

        result shouldBe expected
    }

    @Test
    fun `username 으로 공개 collection 목록을 조회한다`() {
        val expected = listOf(
            FeedCollectionData(
                collectionId = 2L,
                name = "원정 기록",
                postCount = 5L,
                lastPostRuleModifiedAt = LocalDateTime.of(2026, 6, 12, 9, 30, 0),
                thumbnailUrl = "https://cdn.example.com/collections/2-thumb.webp",
                isPublic = true,
            ),
        )
        every { feedCollectionReader.readPublishedAllByUsername("baezzal") } returns expected

        val result = feedService.findCollectionsByUsername("baezzal")

        result shouldBe expected
    }
}
