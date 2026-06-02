package server.post.domain

import global.entity.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
        AttributeOverride(name = "url", column = Column(name = "image_url", nullable = false, length = 2048)),
        AttributeOverride(name = "width", column = Column(name = "image_width")),
        AttributeOverride(name = "height", column = Column(name = "image_height")),
        AttributeOverride(name = "aspectRatio", column = Column(name = "image_aspect_ratio")),
    )
    var originalImage: ImageAsset,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "url", column = Column(name = "thumbnail_url", nullable = false, length = 2048)),
        AttributeOverride(name = "width", column = Column(name = "thumbnail_width")),
        AttributeOverride(name = "height", column = Column(name = "thumbnail_height")),
        AttributeOverride(name = "aspectRatio", column = Column(name = "thumbnail_aspect_ratio")),
    )
    var thumbnailImage: ImageAsset = ImageAsset(),

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", nullable = false, length = 20)
    var thumbnailStatus: ThumbnailStatus = ThumbnailStatus.PENDING,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "team_id")
    var teamId: Long? = null,
) : BaseEntity() {
    val imageUrl: String
        get() = originalImage.url

    val thumbnailUrl: String
        get() = thumbnailImage.url

    fun completeThumbnail(
        originalImage: ImageAsset,
        thumbnailImage: ImageAsset,
    ) {
        this.originalImage = originalImage
        this.thumbnailImage = thumbnailImage
        this.thumbnailStatus = ThumbnailStatus.SUCCESS
    }

    fun failThumbnail() {
        this.thumbnailStatus = ThumbnailStatus.FAIL
    }

    fun updateDescription(description: String) {
        this.description = description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Post) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
