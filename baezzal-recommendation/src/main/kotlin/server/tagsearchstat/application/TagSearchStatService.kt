package server.tagsearchstat.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.tagsearchstat.implementation.TagSearchStatDailyWriter

@Service
class TagSearchStatService(
    private val tagSearchStatDailyWriter: TagSearchStatDailyWriter,
) {
    @Transactional
    fun recordTagSearched(event: TagSearchedEvent) {
        tagSearchStatDailyWriter.incrementSearchCount(
            tagId = event.tagId,
            statDate = event.searchedAt.toLocalDate(),
        )
    }
}
