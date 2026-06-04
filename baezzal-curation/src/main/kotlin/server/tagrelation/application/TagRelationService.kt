package server.tagrelation.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.post.application.PostCreatedEvent
import server.posttag.implementation.PostTagReader
import server.tagrelation.domain.TagRelation
import server.tagrelation.domain.TagRelationType
import server.tagrelation.implementation.TagRelationReader
import server.tagrelation.implementation.TagRelationWriter

@Service
class TagRelationService(
    private val postTagReader: PostTagReader,
    private val tagRelationReader: TagRelationReader,
    private val tagRelationWriter: TagRelationWriter,
) {
    @Transactional
    fun recordPostCreated(event: PostCreatedEvent) {
        val tagIds = postTagReader.readAllByPostId(event.postId)
            .map { it.tagId }
            .distinct()
            .sorted()

        if (tagIds.size < 2) {
            return
        }

        val tagPairs = buildTagPairs(tagIds)
        val existingByPair = tagRelationReader.readAllByRelationTypeAndPairs(
            relationType = TagRelationType.CO_OCCURRENCE,
            tagPairs = tagPairs,
        ).associateBy { it.sourceTagId to it.targetTagId }

        val tagRelations = tagPairs.map { (sourceTagId, targetTagId) ->
            existingByPair[sourceTagId to targetTagId]?.apply {
                increaseScore(CO_OCCURRENCE_DELTA)
            } ?: TagRelation(
                sourceTagId = sourceTagId,
                targetTagId = targetTagId,
                relationType = TagRelationType.CO_OCCURRENCE,
                score = CO_OCCURRENCE_DELTA,
            )
        }

        tagRelationWriter.writeAll(tagRelations)
    }

    private fun buildTagPairs(tagIds: List<Long>): List<Pair<Long, Long>> =
        buildList {
            for (sourceIndex in 0 until tagIds.lastIndex) {
                val sourceTagId = tagIds[sourceIndex]
                for (targetIndex in sourceIndex + 1 until tagIds.size) {
                    add(sourceTagId to tagIds[targetIndex])
                }
            }
        }

    companion object {
        private const val CO_OCCURRENCE_DELTA = 1L
    }
}
