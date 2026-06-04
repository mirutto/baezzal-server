package server.collection.implementation

import org.springframework.stereotype.Component
import server.collection.application.CollectionPostAddedEvent
import server.outbox.TransactionalEventPublisher
import java.time.LocalDateTime

@Component
class CollectionEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {
    fun publishPostAdded(
        userId: Long,
        collectionId: Long,
        postId: Long,
    ) {
        transactionalEventPublisher.publish(
            CollectionPostAddedEvent(
                userId = userId,
                collectionId = collectionId,
                postId = postId,
                addedAt = LocalDateTime.now(),
            ),
        )
    }
}
