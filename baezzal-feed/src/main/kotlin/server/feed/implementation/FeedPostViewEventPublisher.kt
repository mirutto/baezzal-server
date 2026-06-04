package server.feed.implementation

import org.springframework.stereotype.Component
import server.post.implementation.PostEventPublisher

@Component
class FeedPostViewEventPublisher(
    private val postEventPublisher: PostEventPublisher,
) {
    fun publish(
        memberId: Long,
        postId: Long,
    ) {
        postEventPublisher.publishViewed(
            userId = memberId,
            postId = postId,
        )
    }
}
