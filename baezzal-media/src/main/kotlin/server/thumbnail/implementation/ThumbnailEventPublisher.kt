package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.outbox.TransactionalEventPublisher
import server.thumbnail.domain.ThumbnailUpdatedEvent

@Component
class ThumbnailEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher
) {

    fun publishUploaded(
        postId: Long,
        thumbnailUrl: String,
    ) {
        transactionalEventPublisher.publish(
            ThumbnailUpdatedEvent(postId, thumbnailUrl)
        )
    }
}
