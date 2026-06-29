package server.feed.model.post

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "post_engagement_stat_daily")
class FeedPostEngagementStatDaily(
    @Id
    @Column(name = "post_engagement_stat_daily_id", nullable = false)
    val id: Long = 0,

    @Column(name = "post_id", nullable = false)
    val postId: Long = 0,

    @Column(name = "stat_date", nullable = false)
    val statDate: LocalDate = LocalDate.MIN,

    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,

    @Column(name = "collection_added_count", nullable = false)
    val collectionAddedCount: Long = 0,
)
