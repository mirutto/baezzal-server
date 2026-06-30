package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.model.tag.FeedTag
import server.feed.model.tagrelation.FeedTagRelation
import server.feed.model.tagrelation.FeedTagRelationType

@Component
class FeedTagRelationQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readAutocompleteFallbackTags(
        seedTagIds: Collection<Long>,
        excludeTagIds: Collection<Long>,
        limit: Int,
    ): List<TagAutocompleteQueryRow> {
        if (seedTagIds.isEmpty() || limit <= 0) {
            return emptyList()
        }

        val candidates = readTargetTagCandidates(
            seedTagIds = seedTagIds,
            excludeTagIds = excludeTagIds,
        ) + readSourceTagCandidates(
            seedTagIds = seedTagIds,
            excludeTagIds = excludeTagIds,
        )

        return candidates
            .groupBy { it.tagId to it.title }
            .map { (key, rows) ->
                TagRelationCandidateQueryRow(
                    tagId = key.first,
                    title = key.second,
                    score = rows.sumOf(TagRelationCandidateQueryRow::score),
                )
            }.sortedWith(
                compareByDescending<TagRelationCandidateQueryRow> { it.score }
                    .thenBy { it.title }
                    .thenBy { it.tagId }
            ).take(limit)
            .map {
                TagAutocompleteQueryRow(
                    tagId = it.tagId,
                    title = it.title,
                )
            }
    }

    private fun readTargetTagCandidates(
        seedTagIds: Collection<Long>,
        excludeTagIds: Collection<Long>,
    ): List<TagRelationCandidateQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<TagRelationCandidateQueryRow>(
                        path(FeedTag::id),
                        path(FeedTag::title),
                        path(FeedTagRelation::score),
                    ).from(
                        entity(FeedTagRelation::class),
                        join(FeedTag::class).on(path(FeedTagRelation::targetTagId).equal(path(FeedTag::id))),
                    ).whereAnd(
                        path(FeedTagRelation::relationType).eq(FeedTagRelationType.CO_OCCURRENCE),
                        path(FeedTagRelation::sourceTagId).`in`(seedTagIds),
                        path(FeedTagRelation::targetTagId).notIn(excludeTagIds),
                    )
                },
                TagRelationCandidateQueryRow::class.java,
            ).resultList

    private fun readSourceTagCandidates(
        seedTagIds: Collection<Long>,
        excludeTagIds: Collection<Long>,
    ): List<TagRelationCandidateQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<TagRelationCandidateQueryRow>(
                        path(FeedTag::id),
                        path(FeedTag::title),
                        path(FeedTagRelation::score),
                    ).from(
                        entity(FeedTagRelation::class),
                        join(FeedTag::class).on(path(FeedTagRelation::sourceTagId).equal(path(FeedTag::id))),
                    ).whereAnd(
                        path(FeedTagRelation::relationType).eq(FeedTagRelationType.CO_OCCURRENCE),
                        path(FeedTagRelation::targetTagId).`in`(seedTagIds),
                        path(FeedTagRelation::sourceTagId).notIn(excludeTagIds),
                    )
                },
                TagRelationCandidateQueryRow::class.java,
            ).resultList
}
