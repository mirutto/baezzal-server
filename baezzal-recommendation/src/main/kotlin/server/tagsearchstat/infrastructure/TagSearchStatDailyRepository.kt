package server.tagsearchstat.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import server.tagsearchstat.domain.TagSearchStatDaily

interface TagSearchStatDailyRepository : JpaRepository<TagSearchStatDaily, Long> {
    @Modifying
    @Query(
        value = """
            INSERT INTO tag_search_stat_daily (
                tag_id,
                stat_date,
                search_count,
                created_at,
                updated_at
            )
            VALUES (
                :tagId,
                :statDate,
                1,
                CURRENT_TIMESTAMP(6),
                CURRENT_TIMESTAMP(6)
            )
            ON DUPLICATE KEY UPDATE
                search_count = search_count + 1,
                updated_at = CURRENT_TIMESTAMP(6)
        """,
        nativeQuery = true,
    )
    fun incrementSearchCount(
        tagId: Long,
        statDate: LocalDate,
    ): Int
}
