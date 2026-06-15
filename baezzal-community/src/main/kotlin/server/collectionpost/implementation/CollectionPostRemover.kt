package server.collectionpost.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collectionpost.domain.CollectionPost
import server.collectionpost.infrastructure.CollectionPostRepository

@Component
class CollectionPostRemover(
    private val collectionPostRepository: CollectionPostRepository,
) {
    @Transactional
    fun remove(collectionPost: CollectionPost) {
        collectionPostRepository.delete(collectionPost)
    }

    @Transactional
    fun removeAllByCollectionId(collectionId: Long) {
        collectionPostRepository.deleteAllByCollectionId(collectionId)
    }
}
