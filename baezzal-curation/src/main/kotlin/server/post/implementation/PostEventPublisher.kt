package server.post.implementation

import org.springframework.stereotype.Component
import server.outbox.TransactionalEventPublisher
import server.post.application.PostCreatedEvent
import server.post.application.PostViewedEvent
import server.post.domain.Post
import java.time.LocalDateTime

@Component
class PostEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {
    fun publishCreated(post: Post) {
        transactionalEventPublisher.publish(PostCreatedEvent(post.id, post.originalImage.url))
    }

    fun publishViewed(
        userId: Long,
        postId: Long,
    ) {
        transactionalEventPublisher.publish(
            PostViewedEvent(
                userId = userId,
                postId = postId,
                viewedAt = LocalDateTime.now(),
            ),
        )
    }
}
