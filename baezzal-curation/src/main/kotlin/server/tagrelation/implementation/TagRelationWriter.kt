package server.tagrelation.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.tagrelation.domain.TagRelation
import server.tagrelation.infrastructure.TagRelationRepository

@Component
class TagRelationWriter(
    private val tagRelationRepository: TagRelationRepository,
) {
    @Transactional
    fun writeAll(tagRelations: Collection<TagRelation>): List<TagRelation> {
        if (tagRelations.isEmpty()) {
            return emptyList()
        }

        return tagRelationRepository.saveAll(tagRelations)
    }
}
