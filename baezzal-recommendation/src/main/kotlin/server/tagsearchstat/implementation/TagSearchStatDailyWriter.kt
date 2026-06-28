package server.tagsearchstat.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.tagsearchstat.infrastructure.TagSearchStatDailyRepository
import java.time.LocalDate

@Component
class TagSearchStatDailyWriter(
    private val tagSearchStatDailyRepository: TagSearchStatDailyRepository,
) {
    @Transactional
    fun incrementSearchCount(
        tagId: Long,
        statDate: LocalDate,
    ) {
        tagSearchStatDailyRepository.incrementSearchCount(
            tagId = tagId,
            statDate = statDate,
        )
    }
}
