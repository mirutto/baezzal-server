package server.collection.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collection.domain.Collection
import server.collection.infrastructure.CollectionRepository

@Component
class CollectionRemover(
    private val collectionRepository: CollectionRepository,
) {
    @Transactional
    fun remove(collection: Collection) {
        collectionRepository.delete(collection)
    }
}
