package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import global.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.FeedCollectionData
import server.feed.model.collection.FeedCollection
import server.feed.model.collection.FeedCollectionPost
import server.feed.model.member.FeedMember

@Component
class FeedCollectionQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readAllByMemberId(memberId: Long): List<FeedCollectionData> = readCollectionData(
        readCollectionRowsByMemberId(memberId),
    )

    @Transactional(readOnly = true)
    fun readPublishedAllByUsername(username: String): List<FeedCollectionData> =
        readCollectionData(
            readPublishedCollectionRowsByMemberId(readMemberIdByUsername(username)),
        )

    private fun readCollectionData(collectionRows: List<FeedCollectionQueryRow>): List<FeedCollectionData> {
        if (collectionRows.isEmpty()) {
            return emptyList()
        }

        val postCounts = readPostCounts(collectionRows.map(FeedCollectionQueryRow::collectionId))

        return collectionRows.map { collection ->
            FeedCollectionData(
                collectionId = collection.collectionId,
                name = collection.name,
                postCount = postCounts[collection.collectionId] ?: 0L,
                lastPostRuleModifiedAt = collection.lastPostRuleModifiedAt,
                thumbnailUrl = collection.thumbnailUrl,
                isPublic = collection.isPublic,
            )
        }
    }

    private fun readCollectionRowsByMemberId(memberId: Long): List<FeedCollectionQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedCollectionQueryRow>(
                        path(FeedCollection::id),
                        path(FeedCollection::name),
                        path(FeedCollection::lastPostRuleModifiedAt),
                        path(FeedCollection::thumbnailUrl),
                        path(FeedCollection::isPublished),
                    ).from(
                        entity(FeedCollection::class),
                    ).where(
                        path(FeedCollection::memberId).eq(memberId),
                    ).orderBy(
                        path(FeedCollection::lastPostRuleModifiedAt).desc(),
                        path(FeedCollection::id).desc(),
                    )
                },
                FeedCollectionQueryRow::class.java,
            ).resultList

    private fun readPublishedCollectionRowsByMemberId(memberId: Long): List<FeedCollectionQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedCollectionQueryRow>(
                        path(FeedCollection::id),
                        path(FeedCollection::name),
                        path(FeedCollection::lastPostRuleModifiedAt),
                        path(FeedCollection::thumbnailUrl),
                        path(FeedCollection::isPublished),
                    ).from(
                        entity(FeedCollection::class),
                    ).whereAnd(
                        path(FeedCollection::memberId).eq(memberId),
                        path(FeedCollection::isPublished).eq(true),
                    ).orderBy(
                        path(FeedCollection::lastPostRuleModifiedAt).desc(),
                        path(FeedCollection::id).desc(),
                    )
                },
                FeedCollectionQueryRow::class.java,
            ).resultList

    private fun readPostCounts(collectionIds: List<Long>): Map<Long, Long> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedCollectionPostCountQueryRow>(
                        path(FeedCollectionPost::collectionId),
                        count(path(FeedCollectionPost::id)),
                    ).from(
                        entity(FeedCollectionPost::class),
                    ).where(
                        path(FeedCollectionPost::collectionId).`in`(collectionIds),
                    ).groupBy(
                        path(FeedCollectionPost::collectionId),
                    )
                },
                FeedCollectionPostCountQueryRow::class.java,
            ).resultList
            .associate { row -> row.collectionId to row.postCount }

    private fun readMemberIdByUsername(username: String): Long =
        jdslExecutor
            .createQuery(
                jpql {
                    select(
                        path(FeedMember::id),
                    ).from(
                        entity(FeedMember::class),
                    ).where(
                        path(FeedMember::username).eq(username),
                    )
                },
                Long::class.javaObjectType,
            ).resultList
            .firstOrNull()
            ?: throw NotFoundException("회원을 찾을 수 없습니다")
}
