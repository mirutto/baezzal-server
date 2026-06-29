package server.feed.model.tag

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "tag_search_stat_daily")
class FeedTagSearchStatDaily(
    @Id
    @Column(name = "tag_search_stat_daily_id", nullable = false)
    val id: Long = 0,

    @Column(name = "tag_id", nullable = false)
    val tagId: Long = 0,

    @Column(name = "stat_date", nullable = false)
    val statDate: LocalDate = LocalDate.MIN,

    @Column(name = "search_count", nullable = false)
    val searchCount: Long = 0,
)
