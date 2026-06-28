package server.tagsearchstat.application

import java.time.LocalDateTime

data class TagSearchedEvent(
    val tagId: Long,
    val searchedAt: LocalDateTime,
)
