package server.collection.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

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

    @Column(name = "thumbnail_url", nullable = false, length = 2048)
    var thumbnailUrl: String,
) : BaseEntity() {
    fun updateName(name: String) {
        this.name = name
    }

    fun updateThumbnail(thumbnailUrl: String) {
        this.thumbnailUrl = thumbnailUrl
    }

    fun update(
        name: String,
        thumbnailUrl: String,
    ) {
        updateName(name)
        updateThumbnail(thumbnailUrl)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
