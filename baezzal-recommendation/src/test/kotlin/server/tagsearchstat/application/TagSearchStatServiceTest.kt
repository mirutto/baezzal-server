package server.tagsearchstat.application

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.tagsearchstat.implementation.TagSearchStatDailyWriter
import java.time.LocalDate
import java.time.LocalDateTime

class TagSearchStatServiceTest {
    private val tagSearchStatDailyWriter = mockk<TagSearchStatDailyWriter>(relaxed = true)
    private val tagSearchStatService = TagSearchStatService(
        tagSearchStatDailyWriter = tagSearchStatDailyWriter,
    )

    @Test
    fun `태그 검색 시 search count 를 누적한다`() {
        val searchedAt = LocalDateTime.of(2026, 6, 29, 12, 0, 0)
        val statDate = LocalDate.of(2026, 6, 29)

        tagSearchStatService.recordTagSearched(
            TagSearchedEvent(
                tagId = 10L,
                searchedAt = searchedAt,
            ),
        )

        verify(exactly = 1) {
            tagSearchStatDailyWriter.incrementSearchCount(
                tagId = 10L,
                statDate = statDate,
            )
        }
    }
}
