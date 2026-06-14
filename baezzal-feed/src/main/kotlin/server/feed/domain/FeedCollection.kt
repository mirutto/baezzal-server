package server.feed.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "collection")
class FeedCollection(
    @Id
    @Column(name = "collection_id", nullable = false)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "name", nullable = false, length = 100)
    val name: String = "",

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Column(name = "raw_url", nullable = false, length = 2048)
    val rawUrl: String = "",

    @Column(name = "public_url", nullable = false, length = 2048)
    val publicUrl: String = "",

    @Column(name = "thumbnail_url", nullable = false, length = 2048)
    val thumbnailUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: FeedThumbnailStatus = FeedThumbnailStatus.PROCESSING,

    @Column(name = "aspect_ratio", nullable = false)
    val aspectRatio: Double = 1.0,

    @Column(name = "is_custom_thumbnail", nullable = false)
    val isCustomThumbnail: Boolean = false,

    @Column(name = "is_published", nullable = false)
    val isPublished: Boolean = false,

    @Column(
        name = "last_post_rule_modified_at",
        nullable = false,
        columnDefinition = "DATETIME(6)",
    )
    val lastPostRuleModifiedAt: LocalDateTime = LocalDateTime.now(),

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    val createdAt: LocalDateTime? = null,
)
