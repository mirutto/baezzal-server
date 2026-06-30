package server.feed.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.feed.query.DailyPopularTagQueryRow
import server.feed.query.FeedTagQuery
import server.feed.query.FeedTagRelationQuery
import server.feed.query.FeedTagSearchStatDailyQuery
import server.feed.query.TagAutocompleteQueryRow

class FeedTagServiceTest {
    private val feedTagSearchStatDailyQuery = mockk<FeedTagSearchStatDailyQuery>()
    private val feedTagQuery = mockk<FeedTagQuery>()
    private val feedTagRelationQuery = mockk<FeedTagRelationQuery>()
    private val feedTagService = FeedTagService(
        feedTagSearchStatDailyQuery = feedTagSearchStatDailyQuery,
        feedTagQuery = feedTagQuery,
        feedTagRelationQuery = feedTagRelationQuery,
    )

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

        val result = feedTagService.findDailyPopularTags(limit = null)

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

        val result = feedTagService.findDailyPopularTags(limit = null)

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

        feedTagService.findDailyPopularTags(limit = 0)
        feedTagService.findDailyPopularTags(limit = 30)

        verify(exactly = 1) { feedTagSearchStatDailyQuery.readDailyPopularTags(1) }
        verify(exactly = 1) { feedTagSearchStatDailyQuery.readDailyPopularTags(20) }
    }

    @Test
    fun `tag 자동완성을 조회한다`() {
        every {
            feedTagQuery.readAutocompleteByKeyword(
                keyword = "잠",
                limit = 10,
            )
        } returns listOf(
            TagAutocompleteQueryRow(
                tagId = 3L,
                title = "잠실",
            ),
            TagAutocompleteQueryRow(
                tagId = 8L,
                title = "잠실야구장",
            ),
        )
        every {
            feedTagRelationQuery.readAutocompleteFallbackTags(
                seedTagIds = listOf(3L, 8L),
                excludeTagIds = listOf(3L, 8L),
                limit = 8,
            )
        } returns listOf(
            TagAutocompleteQueryRow(
                tagId = 9L,
                title = "서울야구",
            ),
        )

        val result = feedTagService.autocompleteTags(keyword = " 잠 ", limit = null)

        result shouldBe listOf(
            TagAutocompleteData(
                tagId = 3L,
                title = "잠실",
            ),
            TagAutocompleteData(
                tagId = 8L,
                title = "잠실야구장",
            ),
            TagAutocompleteData(
                tagId = 9L,
                title = "서울야구",
            ),
        )
    }

    @Test
    fun `tag 자동완성 결과가 limit 을 채우면 relation 조회를 하지 않는다`() {
        every {
            feedTagQuery.readAutocompleteByKeyword(
                keyword = "잠",
                limit = 2,
            )
        } returns listOf(
            TagAutocompleteQueryRow(
                tagId = 3L,
                title = "잠실",
            ),
            TagAutocompleteQueryRow(
                tagId = 8L,
                title = "잠실야구장",
            ),
        )

        val result = feedTagService.autocompleteTags(keyword = "잠", limit = 2)

        result shouldBe listOf(
            TagAutocompleteData(
                tagId = 3L,
                title = "잠실",
            ),
            TagAutocompleteData(
                tagId = 8L,
                title = "잠실야구장",
            ),
        )
        verify(exactly = 0) { feedTagRelationQuery.readAutocompleteFallbackTags(any(), any(), any()) }
    }

    @Test
    fun `tag 자동완성은 빈 검색어면 빈 목록을 반환한다`() {
        val result = feedTagService.autocompleteTags(keyword = "   ", limit = null)

        result shouldBe emptyList()
        verify(exactly = 0) { feedTagQuery.readAutocompleteByKeyword(any(), any()) }
        verify(exactly = 0) { feedTagRelationQuery.readAutocompleteFallbackTags(any(), any(), any()) }
    }

    @Test
    fun `tag 자동완성 조회 limit 을 보정한다`() {
        every { feedTagQuery.readAutocompleteByKeyword(keyword = "잠", limit = 1) } returns emptyList()
        every { feedTagQuery.readAutocompleteByKeyword(keyword = "잠", limit = 20) } returns emptyList()

        feedTagService.autocompleteTags(keyword = "잠", limit = 0)
        feedTagService.autocompleteTags(keyword = "잠", limit = 30)

        verify(exactly = 1) { feedTagQuery.readAutocompleteByKeyword(keyword = "잠", limit = 1) }
        verify(exactly = 1) { feedTagQuery.readAutocompleteByKeyword(keyword = "잠", limit = 20) }
    }
}
