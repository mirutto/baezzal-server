package server.userinterest.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.userinterest.application.CollectionPostAddedEvent
import server.userinterest.application.PostViewedEvent
import server.userinterest.application.UserInterestService

@Component
class UserInterestEventHandler(
    private val userInterestService: UserInterestService,
) {
    @EventHandler("update-user-tag-interest")
    fun recordPostView(event: PostViewedEvent) {
        userInterestService.recordPostView(event)
    }

    @EventHandler("update-user-tag-interest")
    fun recordCollectionPostAdded(event: CollectionPostAddedEvent) {
        userInterestService.recordCollectionPostAdded(event)
    }
}
