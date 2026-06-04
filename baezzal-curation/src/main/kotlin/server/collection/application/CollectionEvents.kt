package server.collection.application

import java.time.LocalDateTime

data class CollectionPostAddedEvent(
    val userId: Long,
    val collectionId: Long,
    val postId: Long,
    val addedAt: LocalDateTime,
)
