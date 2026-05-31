package server.collection.implementation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.collection.domain.Collection
import server.collection.infrastructure.CollectionRepository

@Component
class CollectionReader(
    private val collectionRepository: CollectionRepository,
) {
    @Transactional(readOnly = true)
    fun readById(collectionId: Long): Collection? = collectionRepository.findByIdOrNull(collectionId)

    @Transactional(readOnly = true)
    fun readByIdAndMemberId(
        collectionId: Long,
        memberId: Long,
    ): Collection? = collectionRepository.findByIdAndMemberId(collectionId, memberId)

    @Transactional(readOnly = true)
    fun readAllByMemberId(memberId: Long): List<Collection> =
        collectionRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)
}
