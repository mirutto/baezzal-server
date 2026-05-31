package server.post.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
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

    @Column(name = "image_url", nullable = false, length = 2048)
    val imageUrl: String,

    @Column(name = "thumbnail_url", nullable = false, length = 2048)
    var thumbnailUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", nullable = false, length = 20)
    var thumbnailStatus: ThumbnailStatus = ThumbnailStatus.PENDING,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "team_id")
    var teamId: Long? = null,
) : BaseEntity() {
    fun completeThumbnail(thumbnailUrl: String) {
        this.thumbnailUrl = thumbnailUrl
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
