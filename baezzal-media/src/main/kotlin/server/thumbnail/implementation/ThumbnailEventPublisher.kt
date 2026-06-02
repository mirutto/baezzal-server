package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.messaging.EventPublisher
import server.thumbnail.applicaiton.ThumbnailUpdatedEvent

@Component
class ThumbnailEventPublisher(
    private val eventPublisher: EventPublisher,
) {
    fun publishUploaded(
        postId: Long,
        thumbnailUrl: String,
    ) {
        eventPublisher.publish(ThumbnailUpdatedEvent(postId, thumbnailUrl))
    }
}
