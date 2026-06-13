package server.collection.presentation

import org.springframework.stereotype.Component
import server.collection.application.CollectionService
import server.member.application.MemberCreatedEvent
import server.messaging.annotation.EventHandler

@Component
class CollectionEventHandler(
    private val collectionService: CollectionService,
) {

    @EventHandler("create-default-collection")
    fun createDefaultCollection(event: MemberCreatedEvent) {
        collectionService.createDefault(event.memberId)
    }
}
