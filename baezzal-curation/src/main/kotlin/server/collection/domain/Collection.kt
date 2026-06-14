package server.collection.domain

import global.entity.BaseEntity
import global.image.ImageVersions
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "collection")
class Collection(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id", nullable = false)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String = "",

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "rawUrl",
            column = Column(name = "raw_url", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "publicUrl",
            column = Column(name = "public_url", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "thumbnailUrl",
            column = Column(name = "thumbnail_url", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "status",
            column = Column(name = "status", nullable = false, length = 20),
        ),
        AttributeOverride(
            name = "aspectRatio",
            column = Column(name = "aspect_ratio", nullable = false),
        ),
    )
    var imageVersions: ImageVersions,

    @Column(name = "is_custom_thumbnail", nullable = false)
    var isCustomThumbnail: Boolean = false,

    @Column(name = "is_published", nullable = false)
    var isPublished: Boolean = false,

    @Column(
        name = "last_post_rule_modified_at",
        nullable = false,
        columnDefinition = "DATETIME(6)",
    )
    var lastPostRuleModifiedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
