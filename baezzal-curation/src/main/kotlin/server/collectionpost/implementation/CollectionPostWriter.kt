package server.collectionpost.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collectionpost.domain.CollectionPost
import server.collectionpost.infrastructure.CollectionPostRepository

@Component
class CollectionPostWriter(
    private val collectionPostRepository: CollectionPostRepository,
) {
    @Transactional
    fun write(collectionPost: CollectionPost): CollectionPost = collectionPostRepository.save(collectionPost)
}
