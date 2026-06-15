package server.userinterest.application

import java.time.LocalDateTime

data class PostViewedEvent(
    val userId: Long,
    val postId: Long,
    val viewedAt: LocalDateTime,
)

data class CollectionPostAddedEvent(
    val userId: Long,
    val collectionId: Long,
    val postId: Long,
    val addedAt: LocalDateTime,
)
