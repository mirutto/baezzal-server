package server.tagrelation.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.post.application.PostCreatedEvent
import server.tagrelation.application.TagRelationService

@Component
class TagRelationEventHandler(
    private val tagRelationService: TagRelationService,
) {
    @EventHandler("update-tag-relation")
    fun recordPostCreated(event: PostCreatedEvent) {
        tagRelationService.recordPostCreated(event)
    }
}
