package server.postengagementstat.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.postengagementstat.application.CollectionPostAddedEvent
import server.postengagementstat.application.PostViewedEvent
import server.postengagementstat.application.PostEngagementService

@Component
class PostEngagementStatEventHandler(
    private val postEngagementService: PostEngagementService,
) {
    @EventHandler("update-post-engagement-stat")
    fun recordPostView(event: PostViewedEvent) {
        postEngagementService.recordPostView(event)
    }

    @EventHandler("update-post-engagement-stat")
    fun recordCollectionPostAdded(event: CollectionPostAddedEvent) {
        postEngagementService.recordCollectionPostAdded(event)
    }
}
