package server.tagrelation.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.tagrelation.domain.TagRelation
import server.tagrelation.domain.TagRelationType

interface TagRelationRepository : JpaRepository<TagRelation, Long> {
    fun findAllByRelationTypeAndSourceTagIdInAndTargetTagIdIn(
        relationType: TagRelationType,
        sourceTagIds: Collection<Long>,
        targetTagIds: Collection<Long>,
    ): List<TagRelation>
}
