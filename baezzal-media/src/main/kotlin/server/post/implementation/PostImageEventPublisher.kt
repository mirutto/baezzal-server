package server.post.implementation

import org.springframework.stereotype.Component
import server.messaging.EventPublisher
import server.post.application.PostImageProcessedEvent

@Component
class PostImageEventPublisher(
    private val eventPublisher: EventPublisher,
) {

    fun publishProcessed(
        postId: Long,
        rawImageUrl: String,
        publicImageUrl: String,
        thumbnailImageUrl: String,
        aspectRatio: Double,
    ) {
        eventPublisher.publish(
            PostImageProcessedEvent(
                postId = postId,
                rawImageUrl = rawImageUrl,
                publicImageUrl = publicImageUrl,
                thumbnailImageUrl = thumbnailImageUrl,
                aspectRatio = aspectRatio,
            )
        )
    }
}
