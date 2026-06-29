package server.feed.query

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.DailyPopularPostCursor
import server.feed.model.post.FeedThumbnailStatus
import java.time.LocalDate
import java.time.ZoneId

@Component
class FeedPostEngagementStatDailyQuery(
    @PersistenceContext
    private val entityManager: EntityManager,
) {
    @Transactional(readOnly = true)
    fun readDailyPopularPosts(
        cursor: DailyPopularPostCursor?,
        limit: Int,
    ): List<DailyPopularPostQueryRow> {
        val jpql = buildString {
            appendLine(
                """
                SELECT new server.feed.query.DailyPopularPostQueryRow(
                    post.id,
                    post.thumbnailImageUrl,
                    post.publicImageUrl,
                    post.imageAspectRatio,
                    COALESCE(stat.viewCount, 0),
                    post.createdAt
                )
                FROM FeedPost post
                LEFT JOIN FeedPostEngagementStatDaily stat
                    ON stat.postId = post.id
                    AND stat.statDate = :today
                WHERE post.thumbnailStatus = :thumbnailStatus
                """.trimIndent(),
            )

            if (cursor != null) {
                appendLine(
                    """
                    AND (
                        COALESCE(stat.viewCount, 0) < :score
                        OR (
                            COALESCE(stat.viewCount, 0) = :score
                            AND post.createdAt < :createdAt
                        )
                        OR (
                            COALESCE(stat.viewCount, 0) = :score
                            AND post.createdAt = :createdAt
                            AND post.id < :postId
                        )
                    )
                    """.trimIndent(),
                )
            }

            append(
                """
                ORDER BY
                    COALESCE(stat.viewCount, 0) DESC,
                    post.createdAt DESC,
                    post.id DESC
                """.trimIndent(),
            )
        }

        return entityManager
            .createQuery(jpql, DailyPopularPostQueryRow::class.java)
            .setParameter("today", today())
            .setParameter("thumbnailStatus", FeedThumbnailStatus.SUCCESS)
            .apply {
                cursor?.let {
                    setParameter("score", it.score)
                    setParameter("createdAt", it.createdAt)
                    setParameter("postId", it.postId)
                }
                maxResults = limit
            }
            .resultList
    }

    private fun today(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
}
