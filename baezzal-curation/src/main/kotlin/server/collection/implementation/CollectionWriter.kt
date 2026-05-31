package server.collection.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collection.domain.Collection
import server.collection.infrastructure.CollectionRepository

@Component
class CollectionWriter(
    private val collectionRepository: CollectionRepository,
) {
    @Transactional
    fun write(collection: Collection): Collection = collectionRepository.save(collection)
}
