package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.application.event.FeedEventPublisher
import server.feed.query.FeedCollectionQuery
import server.feed.query.FeedPostEngagementStatDailyQuery
import server.feed.query.FeedPostQuery
import server.feed.query.FeedTagQuery
import server.feed.query.FeedTagRelationQuery
import server.feed.query.FeedTagSearchStatDailyQuery
import server.feed.query.FeedTeamQuery

@Service
class FeedService(
    private val feedPostQuery: FeedPostQuery,
    private val feedCollectionQuery: FeedCollectionQuery,
    private val feedTeamQuery: FeedTeamQuery,
    private val feedPostEngagementStatDailyQuery: FeedPostEngagementStatDailyQuery,
    private val feedTagSearchStatDailyQuery: FeedTagSearchStatDailyQuery,
    private val feedTagQuery: FeedTagQuery,
    private val feedTagRelationQuery: FeedTagRelationQuery,
    private val feedEventPublisher: FeedEventPublisher,
) {
    @Transactional(readOnly = true)
    fun findDailyPopularPosts(
        cursor: String?,
        limit: Int?,
    ): DailyPopularPostSliceResult {
        val normalizedLimit = normalizeLimit(limit)
        val rows = feedPostEngagementStatDailyQuery.readDailyPopularPosts(
            cursor = cursor?.let(DailyPopularPostCursor::decode),
            limit = normalizedLimit + 1,
        )
        val hasNext = rows.size > normalizedLimit
        val pageRows = rows.take(normalizedLimit)

        return DailyPopularPostSliceResult(
            posts = pageRows.map {
                FeedPostData(
                    postId = it.postId,
                    thumbnailImageUrl = it.thumbnailImageUrl,
                    publicImageUrl = it.publicImageUrl,
                    imageAspectRatio = it.imageAspectRatio,
                )
            },
            hasNext = hasNext,
            nextCursor = pageRows.lastOrNull()
                ?.takeIf { hasNext }
                ?.let {
                    DailyPopularPostCursor(
                        score = it.score,
                        createdAt = it.createdAt,
                        postId = it.postId,
                    ).encode()
                },
        )
    }

    @Transactional(readOnly = true)
    fun findDailyPopularTags(limit: Int?): List<DailyPopularTagData> {
        val normalizedLimit = normalizeLimit(limit)
        return feedTagSearchStatDailyQuery.readDailyPopularTags(normalizedLimit)
            .mapIndexed { index, row ->
                DailyPopularTagData(
                    rank = index + 1,
                    tagId = row.tagId,
                    title = row.title,
                    searchCount = row.searchCount,
                )
            }
    }

    @Transactional(readOnly = true)
    fun autocompleteTags(
        keyword: String,
        limit: Int?,
    ): List<TagAutocompleteData> {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank()) {
            return emptyList()
        }

        val normalizedLimit = normalizeLimit(limit)
        val matchedTags = feedTagQuery.readAutocompleteByKeyword(
            keyword = normalizedKeyword,
            limit = normalizedLimit,
        )
        val fallbackTags = if (matchedTags.isEmpty() || matchedTags.size >= normalizedLimit) {
            emptyList()
        } else {
            feedTagRelationQuery.readAutocompleteFallbackTags(
                seedTagIds = matchedTags.map { it.tagId },
                excludeTagIds = matchedTags.map { it.tagId },
                limit = normalizedLimit - matchedTags.size,
            )
        }

        return (matchedTags + fallbackTags).map {
            TagAutocompleteData(
                tagId = it.tagId,
                title = it.title,
            )
        }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<FeedPostData> = feedPostQuery.readAll()

    @Transactional(readOnly = true)
    fun findMine(memberId: Long): List<FeedPostData> = feedPostQuery.readAllByMemberId(memberId)

    @Transactional(readOnly = true)
    fun findByUsername(username: String): List<FeedPostData> = feedPostQuery.readAllByUsername(username)

    @Transactional(readOnly = true)
    fun findTeams(): List<FeedTeamSummaryData> = feedTeamQuery.readTeams()

    @Transactional(readOnly = true)
    fun findMyCollections(memberId: Long): List<FeedCollectionData> = feedCollectionQuery.readAllByMemberId(memberId)

    @Transactional(readOnly = true)
    fun findCollectionsByUsername(username: String): List<FeedCollectionData> =
        feedCollectionQuery.readPublishedAllByUsername(username)

    @Transactional(readOnly = true)
    fun findById(
        postId: Long,
        memberId: Long,
    ): FeedPostDetailData {
        val post = feedPostQuery.readDetail(postId)
        feedEventPublisher.publishPostViewed(memberId = memberId, postId = postId)
        return post
    }

    private fun normalizeLimit(limit: Int?): Int = (limit ?: DEFAULT_DAILY_POPULAR_LIMIT).coerceIn(1, 20)

    companion object {
        private const val DEFAULT_DAILY_POPULAR_LIMIT = 10
    }
}
