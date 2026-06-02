package server.feed.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    val imageUrl: String = "",

    @Column(name = "image_width")
    val imageWidth: Int? = null,

    @Column(name = "image_height")
    val imageHeight: Int? = null,

    @Column(name = "image_aspect_ratio")
    val imageAspectRatio: Double? = null,

    @Column(name = "thumbnail_url", nullable = false, length = 2048)
    val thumbnailUrl: String = "",

    @Column(name = "thumbnail_width")
    val thumbnailWidth: Int? = null,

    @Column(name = "thumbnail_height")
    val thumbnailHeight: Int? = null,

    @Column(name = "thumbnail_aspect_ratio")
    val thumbnailAspectRatio: Double? = null,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    val createdAt: LocalDateTime? = null,
)
