package server.tagrelation.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.tagrelation.domain.TagRelation
import server.tagrelation.domain.TagRelationType
import server.tagrelation.infrastructure.TagRelationRepository

@Component
class TagRelationReader(
    private val tagRelationRepository: TagRelationRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByRelationTypeAndPairs(
        relationType: TagRelationType,
        tagPairs: Collection<Pair<Long, Long>>,
    ): List<TagRelation> {
        if (tagPairs.isEmpty()) {
            return emptyList()
        }

        val sourceTagIds = tagPairs.map { it.first }.distinct()
        val targetTagIds = tagPairs.map { it.second }.distinct()

        return tagRelationRepository.findAllByRelationTypeAndSourceTagIdInAndTargetTagIdIn(
            relationType = relationType,
            sourceTagIds = sourceTagIds,
            targetTagIds = targetTagIds,
        )
    }
}
