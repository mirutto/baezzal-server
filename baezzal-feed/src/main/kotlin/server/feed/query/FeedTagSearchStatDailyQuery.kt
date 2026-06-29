package server.feed.query

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Component
class FeedTagSearchStatDailyQuery(
    @PersistenceContext
    private val entityManager: EntityManager,
) {
    @Transactional(readOnly = true)
    fun readDailyPopularTags(limit: Int): List<DailyPopularTagQueryRow> =
        entityManager
            .createQuery(
                """
                SELECT new server.feed.query.DailyPopularTagQueryRow(
                    tag.id,
                    tag.title,
                    COALESCE(stat.searchCount, 0)
                )
                FROM FeedTag tag
                LEFT JOIN FeedTagSearchStatDaily stat
                    ON stat.tagId = tag.id
                    AND stat.statDate = :today
                ORDER BY
                    COALESCE(stat.searchCount, 0) DESC,
                    tag.createdAt DESC,
                    tag.id DESC
                """.trimIndent(),
                DailyPopularTagQueryRow::class.java,
            ).setParameter("today", today())
            .apply {
                maxResults = limit
            }.resultList

    private fun today(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
}
