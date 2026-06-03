package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.implementation.FeedReader
import server.feed.implementation.FeedTeamReader

@Service
class FeedService(
    private val feedReader: FeedReader,
    private val feedTeamReader: FeedTeamReader,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<FeedPostData> = feedReader.readAll()

    @Transactional(readOnly = true)
    fun findTeams(): List<FeedTeamSummaryData> = feedTeamReader.readTeams()

    @Transactional(readOnly = true)
    fun findById(postId: Long): FeedPostDetailData = feedReader.readDetail(postId)
}
