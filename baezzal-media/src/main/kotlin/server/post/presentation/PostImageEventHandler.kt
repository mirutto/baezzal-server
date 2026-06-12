package server.post.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.post.application.PostCreatedEvent
import server.post.application.PostImageService

@Component
class PostImageEventHandler(
    private val postImageService: PostImageService
) {

    @EventHandler("create-thumbnail")
    fun processPostImage(event: PostCreatedEvent) {
        postImageService.proceed(
            event.postId,
            event.imageUrl,
        )
    }
}
