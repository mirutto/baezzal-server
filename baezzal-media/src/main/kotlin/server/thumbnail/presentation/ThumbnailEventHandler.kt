package server.thumbnail.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.thumbnail.applicaiton.ThumbnailService
import server.thumbnail.domain.PostCreatedEvent

@Component
class ThumbnailEventHandler(
    private val thumbnailService: ThumbnailService,
) {

    @EventHandler("create-thumbnail")
    fun createThumbnail(event: PostCreatedEvent) {
        thumbnailService.createThumbnail(
            event.postId,
            event.imageUrl
        )
    }
}
