package server.feed.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.feed.application.event.FeedEventPublisher
import server.feed.query.DailyPopularPostQueryRow
import server.feed.query.DailyPopularTagQueryRow
import server.feed.query.FeedCollectionQuery
import server.feed.query.FeedPostEngagementStatDailyQuery
import server.feed.query.FeedPostQuery
import server.feed.query.FeedTagSearchStatDailyQuery
import server.feed.query.FeedTeamQuery
import java.time.LocalDateTime

class FeedServiceTest {
    private val feedPostQuery = mockk<FeedPostQuery>()
    private val feedCollectionQuery = mockk<FeedCollectionQuery>()
    private val feedTeamQuery = mockk<FeedTeamQuery>()
    private val feedPostEngagementStatDailyQuery = mockk<FeedPostEngagementStatDailyQuery>()
    private val feedTagSearchStatDailyQuery = mockk<FeedTagSearchStatDailyQuery>()
    private val feedEventPublisher = mockk<FeedEventPublisher>()
    private val feedService = FeedService(
        feedPostQuery = feedPostQuery,
        feedCollectionQuery = feedCollectionQuery,
        feedTeamQuery = feedTeamQuery,
        feedPostEngagementStatDailyQuery = feedPostEngagementStatDailyQuery,
        feedTagSearchStatDailyQuery = feedTagSearchStatDailyQuery,
        feedEventPublisher = feedEventPublisher,
    )

    @Test
    fun `일간 인기 게시글을 조회한다`() {
        every {
            feedPostEngagementStatDailyQuery.readDailyPopularPosts(
                cursor = null,
                limit = 11,
            )
        } returns listOf(
            DailyPopularPostQueryRow(
                postId = 11L,
                thumbnailImageUrl = "https://cdn.example.com/posts/11-thumb.webp",
                publicImageUrl = "https://cdn.example.com/posts/11.webp",
                imageAspectRatio = 1.4,
                score = 15L,
                createdAt = LocalDateTime.of(2026, 6, 29, 18, 0, 0),
            ),
            DailyPopularPostQueryRow(
                postId = 21L,
                thumbnailImageUrl = "https://cdn.example.com/posts/21-thumb.webp",
                publicImageUrl = "https://cdn.example.com/posts/21.webp",
                imageAspectRatio = 1.2,
                score = 0L,
                createdAt = LocalDateTime.of(2026, 6, 29, 17, 0, 0),
            ),
        )

        val result = feedService.findDailyPopularPosts(cursor = null, limit = null)

        result shouldBe DailyPopularPostSliceResult(
            posts = listOf(
                FeedPostData(
                    postId = 11L,
                    thumbnailImageUrl = "https://cdn.example.com/posts/11-thumb.webp",
                    publicImageUrl = "https://cdn.example.com/posts/11.webp",
                    imageAspectRatio = 1.4,
                ),
                FeedPostData(
                    postId = 21L,
                    thumbnailImageUrl = "https://cdn.example.com/posts/21-thumb.webp",
                    publicImageUrl = "https://cdn.example.com/posts/21.webp",
                    imageAspectRatio = 1.2,
                ),
            ),
            hasNext = false,
            nextCursor = null,
        )
    }

    @Test
    fun `일간 인기 게시글 조회 limit 을 보정한다`() {
        every { feedPostEngagementStatDailyQuery.readDailyPopularPosts(cursor = null, limit = 2) } returns emptyList()
        every { feedPostEngagementStatDailyQuery.readDailyPopularPosts(cursor = null, limit = 21) } returns emptyList()

        feedService.findDailyPopularPosts(cursor = null, limit = 0)
        feedService.findDailyPopularPosts(cursor = null, limit = 30)

        verify(exactly = 1) { feedPostEngagementStatDailyQuery.readDailyPopularPosts(cursor = null, limit = 2) }
        verify(exactly = 1) { feedPostEngagementStatDailyQuery.readDailyPopularPosts(cursor = null, limit = 21) }
    }

    @Test
    fun `일간 인기 게시글 다음 커서를 반환한다`() {
        val cursor = encodedCursor(
            score = 200L,
            createdAt = LocalDateTime.of(2026, 6, 29, 19, 0, 0),
            postId = 99L,
        )
        every {
            feedPostEngagementStatDailyQuery.readDailyPopularPosts(
                cursor = DailyPopularPostCursor(
                    score = 200L,
                    createdAt = LocalDateTime.of(2026, 6, 29, 19, 0, 0),
                    postId = 99L,
                ),
                limit = 3,
            )
        } returns dailyPopularRows()

        val result = feedService.findDailyPopularPosts(cursor = cursor, limit = 2)

        result shouldBe DailyPopularPostSliceResult(
            posts = listOf(
                FeedPostData(
                    postId = 11L,
                    thumbnailImageUrl = "https://cdn.example.com/posts/11-thumb.webp",
                    publicImageUrl = "https://cdn.example.com/posts/11.webp",
                    imageAspectRatio = 1.4,
                ),
                FeedPostData(
                    postId = 21L,
                    thumbnailImageUrl = "https://cdn.example.com/posts/21-thumb.webp",
                    publicImageUrl = "https://cdn.example.com/posts/21.webp",
                    imageAspectRatio = 1.2,
                ),
            ),
            hasNext = true,
            nextCursor = encodedCursor(
                score = 95L,
                createdAt = LocalDateTime.of(2026, 6, 29, 17, 30, 0),
                postId = 21L,
            ),
        )
    }

    @Test
    fun `일간 인기 태그를 조회한다`() {
        every { feedTagSearchStatDailyQuery.readDailyPopularTags(10) } returns listOf(
            DailyPopularTagQueryRow(
                tagId = 3L,
                title = "잠실",
                searchCount = 12L,
            ),
            DailyPopularTagQueryRow(
                tagId = 8L,
                title = "엘지트윈스",
                searchCount = 8L,
            ),
        )

        val result = feedService.findDailyPopularTags(limit = null)

        result shouldBe listOf(
            DailyPopularTagData(
                rank = 1,
                tagId = 3L,
                title = "잠실",
                searchCount = 12L,
            ),
            DailyPopularTagData(
                rank = 2,
                tagId = 8L,
                title = "엘지트윈스",
                searchCount = 8L,
            ),
        )
    }

    @Test
    fun `일간 인기 태그는 통계가 없으면 최신 태그도 내려준다`() {
        every { feedTagSearchStatDailyQuery.readDailyPopularTags(10) } returns listOf(
            DailyPopularTagQueryRow(
                tagId = 18L,
                title = "신규태그",
                searchCount = 0L,
            ),
            DailyPopularTagQueryRow(
                tagId = 17L,
                title = "방금생김",
                searchCount = 0L,
            ),
        )

        val result = feedService.findDailyPopularTags(limit = null)

        result shouldBe listOf(
            DailyPopularTagData(
                rank = 1,
                tagId = 18L,
                title = "신규태그",
                searchCount = 0L,
            ),
            DailyPopularTagData(
                rank = 2,
                tagId = 17L,
                title = "방금생김",
                searchCount = 0L,
            ),
        )
    }

    @Test
    fun `일간 인기 태그 조회 limit 을 보정한다`() {
        every { feedTagSearchStatDailyQuery.readDailyPopularTags(1) } returns emptyList()
        every { feedTagSearchStatDailyQuery.readDailyPopularTags(20) } returns emptyList()

        feedService.findDailyPopularTags(limit = 0)
        feedService.findDailyPopularTags(limit = 30)

        verify(exactly = 1) { feedTagSearchStatDailyQuery.readDailyPopularTags(1) }
        verify(exactly = 1) { feedTagSearchStatDailyQuery.readDailyPopularTags(20) }
    }

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

    private fun dailyPopularRows(): List<DailyPopularPostQueryRow> = listOf(
        DailyPopularPostQueryRow(
            postId = 11L,
            thumbnailImageUrl = "https://cdn.example.com/posts/11-thumb.webp",
            publicImageUrl = "https://cdn.example.com/posts/11.webp",
            imageAspectRatio = 1.4,
            score = 120L,
            createdAt = LocalDateTime.of(2026, 6, 29, 18, 0, 0),
        ),
        DailyPopularPostQueryRow(
            postId = 21L,
            thumbnailImageUrl = "https://cdn.example.com/posts/21-thumb.webp",
            publicImageUrl = "https://cdn.example.com/posts/21.webp",
            imageAspectRatio = 1.2,
            score = 95L,
            createdAt = LocalDateTime.of(2026, 6, 29, 17, 30, 0),
        ),
        DailyPopularPostQueryRow(
            postId = 31L,
            thumbnailImageUrl = "https://cdn.example.com/posts/31-thumb.webp",
            publicImageUrl = "https://cdn.example.com/posts/31.webp",
            imageAspectRatio = 1.1,
            score = 70L,
            createdAt = LocalDateTime.of(2026, 6, 29, 17, 0, 0),
        ),
    )

    private fun encodedCursor(
        score: Long,
        createdAt: LocalDateTime,
        postId: Long,
    ): String = DailyPopularPostCursor(
        score = score,
        createdAt = createdAt,
        postId = postId,
    ).encode()
}
