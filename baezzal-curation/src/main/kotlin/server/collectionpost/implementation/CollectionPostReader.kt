package server.collectionpost.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collectionpost.domain.CollectionPost
import server.collectionpost.infrastructure.CollectionPostRepository

@Component
class CollectionPostReader(
    private val collectionPostRepository: CollectionPostRepository,
) {
    @Transactional(readOnly = true)
    fun exists(
        collectionId: Long,
        postId: Long,
    ): Boolean = collectionPostRepository.existsByCollectionIdAndPostId(collectionId, postId)

    @Transactional(readOnly = true)
    fun readByCollectionIdAndPostId(
        collectionId: Long,
        postId: Long,
    ): CollectionPost? = collectionPostRepository.findByCollectionIdAndPostId(collectionId, postId)
}
