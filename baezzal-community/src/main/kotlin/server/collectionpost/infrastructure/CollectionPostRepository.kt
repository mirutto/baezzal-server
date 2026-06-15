package server.collectionpost.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.collectionpost.domain.CollectionPost

interface CollectionPostRepository : JpaRepository<CollectionPost, Long> {
    fun deleteAllByCollectionId(collectionId: Long)

    fun existsByCollectionIdAndPostId(
        collectionId: Long,
        postId: Long,
    ): Boolean

    fun findByCollectionIdAndPostId(
        collectionId: Long,
        postId: Long,
    ): CollectionPost?
}
