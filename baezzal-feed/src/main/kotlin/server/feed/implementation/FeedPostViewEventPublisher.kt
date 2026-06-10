package server.feed.implementation

import org.springframework.stereotype.Component
import server.feed.application.PostViewedEvent
import server.messaging.EventPublisher
import java.time.LocalDateTime

@Component
class FeedPostViewEventPublisher(
    private val eventPublisher: EventPublisher,
) {
    fun publish(
        memberId: Long,
        postId: Long,
    ) {
        eventPublisher.publish(
            PostViewedEvent(
                userId = memberId,
                postId = postId,
                viewedAt = LocalDateTime.now(),
            )
        )
    }
}
