package server.feed.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.feed.application.event.FeedEventPublisher
import server.feed.query.FeedCollectionQuery
import server.feed.query.FeedPostQuery
import server.feed.query.FeedTeamQuery
import java.time.LocalDateTime

class FeedServiceTest {
    private val feedPostQuery = mockk<FeedPostQuery>()
    private val feedCollectionQuery = mockk<FeedCollectionQuery>()
    private val feedTeamQuery = mockk<FeedTeamQuery>()
    private val feedEventPublisher = mockk<FeedEventPublisher>()
    private val feedService = FeedService(
        feedPostQuery = feedPostQuery,
        feedCollectionQuery = feedCollectionQuery,
        feedTeamQuery = feedTeamQuery,
        feedEventPublisher = feedEventPublisher,
    )

    @Test
    fun `team 목록을 조회한다`() {
        val expected = listOf(
            FeedTeamSummaryData(
                teamCode = "LG",
                name = "LG 트윈스",
                postCount = 4L,
                thumbnailUrls = listOf(
                    "https://cdn.example.com/posts/lg-1.webp",
                    "https://cdn.example.com/posts/lg-2.webp",
                ),
            ),
        )
        every { feedTeamQuery.readTeams() } returns expected

        val result = feedService.findTeams()

        result shouldBe expected
    }

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
        every { feedCollectionQuery.readAllByMemberId(7L) } returns expected

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
        every { feedCollectionQuery.readPublishedAllByUsername("baezzal") } returns expected

        val result = feedService.findCollectionsByUsername("baezzal")

        result shouldBe expected
    }

    @Test
    fun `post 상세를 조회한다`() {
        val expected = FeedPostDetailData(
            postId = 1L,
            rawImageUrl = "https://cdn.example.com/posts/1-raw.webp",
            publicImageUrl = "https://cdn.example.com/posts/1.webp",
            imageAspectRatio = 1.25,
            status = "COMPLETED",
            author = FeedAuthorData(
                memberId = 7L,
                nickname = "배짤이",
                username = "baezzal",
                thumbnailProfileImage = "https://cdn.example.com/profiles/1.webp",
                preferredTeam = FeedTeamData(teamCode = "LG", name = "LG 트윈스"),
            ),
            description = "직관 기록",
            tagTitles = listOf("잠실"),
            collectionPostCount = 2L,
        )
        every { feedPostQuery.readDetail(1L) } returns expected
        every { feedEventPublisher.publishPostViewed(7L, 1L) } returns Unit

        val result = feedService.findById(postId = 1L, memberId = 7L)

        result shouldBe expected
        verify(exactly = 1) { feedEventPublisher.publishPostViewed(7L, 1L) }
    }
}
