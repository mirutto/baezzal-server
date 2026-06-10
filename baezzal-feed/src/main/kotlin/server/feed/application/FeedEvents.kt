package server.feed.application

import java.time.LocalDateTime

data class PostViewedEvent(
    val userId: Long,
    val postId: Long,
    val viewedAt: LocalDateTime,
)
