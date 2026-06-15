package server.collection.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.collection.domain.Collection

interface CollectionRepository : JpaRepository<Collection, Long> {
    fun findByIdAndMemberId(
        collectionId: Long,
        memberId: Long,
    ): Collection?

    fun findAllByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Collection>
}
