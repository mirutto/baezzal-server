package server.collectionpost.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "collection_post",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_collection_post_collection_id_post_id",
            columnNames = ["collection_id", "post_id"],
        ),
    ],
)
class CollectionPost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_post_id", nullable = false)
    val id: Long = 0,

    @Column(name = "collection_id", nullable = false)
    val collectionId: Long,

    @Column(name = "post_id", nullable = false)
    val postId: Long,
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionPost) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
