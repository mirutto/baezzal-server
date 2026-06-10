package server.post.implementation

import org.springframework.stereotype.Component
import server.messaging.EventPublisher
import server.post.application.PostCreatedEvent
import server.post.domain.Post

@Component
class PostEventPublisher(
    private val eventPublisher: EventPublisher,
) {
    fun publishCreated(post: Post) {
        eventPublisher.publish(PostCreatedEvent(post.id, post.originalImage.url))
    }
}
