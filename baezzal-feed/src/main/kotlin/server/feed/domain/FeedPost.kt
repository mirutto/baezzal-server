package server.feed.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "post")
class FeedPost(
    @Id
    @Column(name = "post_id", nullable = false)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "image_url", nullable = false, length = 2048)
    val rawImageUrl: String = "",

    @Column(name = "public_url", nullable = false, length = 2048)
    val publicImageUrl: String = "",

    @Column(name = "image_aspect_ratio")
    val imageAspectRatio: Double = 1.0,

    @Column(name = "thumbnail_url", nullable = false, length = 2048)
    val thumbnailImageUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", nullable = false, length = 20)
    val thumbnailStatus: FeedThumbnailStatus = FeedThumbnailStatus.PROCESSING,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    val createdAt: LocalDateTime? = null,
)
