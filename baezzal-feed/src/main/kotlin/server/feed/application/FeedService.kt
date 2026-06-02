package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.implementation.FeedReader

@Service
class FeedService(
    private val feedReader: FeedReader,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<FeedPostData> = feedReader.readAll()

    @Transactional(readOnly = true)
    fun findById(postId: Long): FeedPostDetailData = feedReader.readDetail(postId)
}
