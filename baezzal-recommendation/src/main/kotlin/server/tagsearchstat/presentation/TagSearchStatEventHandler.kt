package server.tagsearchstat.presentation

import org.springframework.stereotype.Component
import server.messaging.annotation.EventHandler
import server.tagsearchstat.application.TagSearchStatService
import server.tagsearchstat.application.TagSearchedEvent

@Component
class TagSearchStatEventHandler(
    private val tagSearchStatService: TagSearchStatService,
) {
    @EventHandler("update-tag-search-stat")
    fun recordTagSearched(event: TagSearchedEvent) {
        tagSearchStatService.recordTagSearched(event)
    }
}
