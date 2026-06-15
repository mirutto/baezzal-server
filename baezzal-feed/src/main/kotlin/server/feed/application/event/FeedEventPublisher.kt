package server.feed.application.event

import org.springframework.stereotype.Component
import server.feed.application.PostViewedEvent
import server.messaging.EventPublisher
import java.time.LocalDateTime

@Component
class FeedEventPublisher(
    private val eventPublisher: EventPublisher,
) {
    fun publishPostViewed(
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
