package server.post.implementation

import org.springframework.stereotype.Component
import server.outbox.TransactionalEventPublisher
import server.post.domain.Post

@Component
class PostEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher
) {

    fun publishCreated(post: Post) {
        transactionalEventPublisher.publish(PostCreatedEvent(post.id, post.imageUrl))
    }
}
