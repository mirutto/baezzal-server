package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.application.event.FeedEventPublisher
import server.feed.query.FeedCollectionQuery
import server.feed.query.FeedPostQuery
import server.feed.query.FeedPostViewCache
import server.feed.query.FeedTeamQuery

@Service
class FeedService(
    private val feedPostQuery: FeedPostQuery,
    private val feedCollectionQuery: FeedCollectionQuery,
    private val feedTeamQuery: FeedTeamQuery,
    private val feedPostViewCache: FeedPostViewCache,
    private val feedEventPublisher: FeedEventPublisher,
) {
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
        memberId: Long?,
    ): FeedPostDetailData {
        val post = feedPostQuery.readDetail(postId)
        val cachedViewCount = feedPostViewCache.recordView(postId)
        memberId?.let { feedEventPublisher.publishPostViewed(memberId = it, postId = postId) }
        return post.copy(viewCount = post.viewCount + cachedViewCount)
    }
}
