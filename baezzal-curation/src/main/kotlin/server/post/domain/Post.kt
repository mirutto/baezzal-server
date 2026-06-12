package server.post.domain

import global.entity.BaseEntity
import global.image.ImageStatus
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

@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "rawUrl",
            column = Column(name = "image_url", nullable = false, length = 2048),
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
            column = Column(name = "thumbnail_status", nullable = false, length = 20),
        ),
        AttributeOverride(name = "aspectRatio", column = Column(name = "image_aspect_ratio")),
    )
    var image: ImageVersions,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "team_id")
    var teamId: Long? = null,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,
) : BaseEntity() {
    val rawImageUrl: String
        get() = image.rawUrl

    val imageUrl: String
        get() = image.publicUrl.ifBlank { image.rawUrl }

    val thumbnailUrl: String
        get() = image.thumbnailUrl

    val imageStatus: ImageStatus
        get() = image.status

    fun completeImage(
        publicUrl: String,
        thumbnailUrl: String,
        aspectRatio: Double,
    ) {
        this.image = image.complete(
            publicUrl = publicUrl,
            thumbnailUrl = thumbnailUrl,
            aspectRatio = aspectRatio,
        )
    }

    fun failImage() {
        this.image = image.fail()
    }

    fun updateDescription(description: String) {
        this.description = description
    }

    fun increaseViewCount(delta: Long) {
        this.viewCount += delta
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Post) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
