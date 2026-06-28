package server.postengagementstat.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import server.postengagementstat.domain.PostEngagementStatDaily

interface PostEngagementStatDailyRepository : JpaRepository<PostEngagementStatDaily, Long> {
    @Modifying
    @Query(
        value = """
            INSERT INTO post_engagement_stat_daily (
                post_id,
                stat_date,
                view_count,
                collection_added_count,
                created_at,
                updated_at
            )
            VALUES (
                :postId,
                :statDate,
                1,
                0,
                CURRENT_TIMESTAMP(6),
                CURRENT_TIMESTAMP(6)
            )
            ON DUPLICATE KEY UPDATE
                view_count = view_count + 1,
                updated_at = CURRENT_TIMESTAMP(6)
        """,
        nativeQuery = true,
    )
    fun incrementViewCount(
        postId: Long,
        statDate: LocalDate,
    ): Int

    @Modifying
    @Query(
        value = """
            INSERT INTO post_engagement_stat_daily (
                post_id,
                stat_date,
                view_count,
                collection_added_count,
                created_at,
                updated_at
            )
            VALUES (
                :postId,
                :statDate,
                0,
                1,
                CURRENT_TIMESTAMP(6),
                CURRENT_TIMESTAMP(6)
            )
            ON DUPLICATE KEY UPDATE
                collection_added_count = collection_added_count + 1,
                updated_at = CURRENT_TIMESTAMP(6)
        """,
        nativeQuery = true,
    )
    fun incrementCollectionAddedCount(
        postId: Long,
        statDate: LocalDate,
    ): Int
}
