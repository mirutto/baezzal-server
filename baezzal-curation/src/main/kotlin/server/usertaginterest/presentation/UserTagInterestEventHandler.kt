package server.usertaginterest.presentation

import org.springframework.stereotype.Component
import server.collection.application.CollectionPostAddedEvent
import server.messaging.annotation.EventHandler
import server.post.application.PostViewedEvent
import server.usertaginterest.application.UserTagInterestService

@Component
class UserTagInterestEventHandler(
    private val userTagInterestService: UserTagInterestService,
) {
    @EventHandler("update-user-tag-interest")
    fun recordPostView(event: PostViewedEvent) {
        userTagInterestService.recordPostView(event)
    }

    @EventHandler("update-user-tag-interest")
    fun recordCollectionPostAdded(event: CollectionPostAddedEvent) {
        userTagInterestService.recordCollectionPostAdded(event)
    }
}
