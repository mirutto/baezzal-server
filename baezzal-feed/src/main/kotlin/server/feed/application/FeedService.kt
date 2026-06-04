package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.implementation.FeedReader
import server.feed.implementation.FeedPostViewEventPublisher
import server.feed.implementation.FeedPostViewRecorder
import server.feed.implementation.FeedTeamReader

@Service
class FeedService(
    private val feedReader: FeedReader,
    private val feedTeamReader: FeedTeamReader,
    private val feedPostViewRecorder: FeedPostViewRecorder,
    private val feedPostViewEventPublisher: FeedPostViewEventPublisher,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<FeedPostData> = feedReader.readAll()

    @Transactional(readOnly = true)
    fun findTeams(): List<FeedTeamSummaryData> = feedTeamReader.readTeams()

    @Transactional(readOnly = true)
    fun findById(
        postId: Long,
        memberId: Long?,
    ): FeedPostDetailData {
        val post = feedReader.readDetail(postId)
        val cachedViewCount = feedPostViewRecorder.recordView(postId)
        memberId?.let { feedPostViewEventPublisher.publish(memberId = it, postId = postId) }
        return post.copy(viewCount = post.viewCount + cachedViewCount)
    }
}
