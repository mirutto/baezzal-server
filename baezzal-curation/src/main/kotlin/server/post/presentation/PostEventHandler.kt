package server.post.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.post.application.MediaUploadUrlIssuedEvent
import server.post.application.PostService
import server.post.application.ThumbnailUpdatedEvent

@Component
class PostEventHandler(
    private val postService: PostService,
) {

    @EventHandler("update-thumbnail")
    fun updateThumbnail(event: ThumbnailUpdatedEvent) {
        postService.updateThumbnail(
            postId = event.postId,
            thumbnailUrl = event.thumbnailUrl,
        )
    }

    @EventHandler("record-post-image-url")
    fun recordIssuedImageUrl(event: MediaUploadUrlIssuedEvent) {
        postService.recordIssuedImageUrl(event)
    }
}
