package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.messaging.EventPublisher
import server.thumbnail.applicaiton.ImageAssetEvent
import server.thumbnail.applicaiton.ThumbnailUpdatedEvent

@Component
class ThumbnailEventPublisher(
    private val eventPublisher: EventPublisher,
) {
    fun publishUploaded(
        postId: Long,
        originalImage: ImageAssetEvent,
        thumbnailImage: ImageAssetEvent,
    ) {
        eventPublisher.publish(ThumbnailUpdatedEvent(postId, originalImage, thumbnailImage))
    }
}
