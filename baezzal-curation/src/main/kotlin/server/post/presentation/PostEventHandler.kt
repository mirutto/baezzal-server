package server.post.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.post.application.PostImageProcessedEvent
import server.post.application.PostService

@Component
class PostEventHandler(
    private val postService: PostService,
) {

    @EventHandler("post-image-processed")
    fun updateImage(event: PostImageProcessedEvent) {
        postService.updateImage(event)
    }
}
