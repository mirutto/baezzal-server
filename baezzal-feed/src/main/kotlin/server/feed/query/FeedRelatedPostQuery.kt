package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import global.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.DailyPopularPostCursor
import server.feed.model.post.FeedPost
import server.feed.model.post.FeedPostEngagementStatDaily
import server.feed.model.post.FeedThumbnailStatus
import server.feed.model.posttag.FeedPostTag
import server.feed.model.tagrelation.FeedTagRelation
import server.feed.model.tagrelation.FeedTagRelationType
import server.feed.model.team.FeedTeam
import java.time.LocalDate
import java.time.ZoneId

@Component
class FeedRelatedPostQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readRelatedPostsByTeamCode(
        teamCode: String,
        cursor: DailyPopularPostCursor?,
        limit: Int,
    ): List<DailyPopularPostQueryRow> {
        requireTeamByCode(teamCode)

        val seedTagIds = readSeedTagIdsByTeamCode(teamCode)
        if (seedTagIds.isEmpty()) {
            return emptyList()
        }

        val relatedTagIds = readRelatedTagIds(seedTagIds)
        val allTagIds = (seedTagIds + relatedTagIds).distinct()

        return readPostsByTagIds(
            tagIds = allTagIds,
            cursor = cursor,
            limit = limit,
        )
    }

    private fun requireTeamByCode(teamCode: String) {
        val count = jdslExecutor
            .createQuery(
                jpql {
                    select(count(path(FeedTeam::id)))
                        .from(entity(FeedTeam::class))
                        .where(path(FeedTeam::code).eq(teamCode))
                },
                Long::class.javaObjectType,
                limit = 1,
            ).singleResult
            .toLong()

        if (count == 0L) {
            throw NotFoundException("팀을 찾을 수 없습니다")
        }
    }

    private fun readSeedTagIdsByTeamCode(teamCode: String): List<Long> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectDistinct<Long>(
                        path(FeedPostTag::tagId),
                    ).from(
                        entity(FeedTeam::class),
                        join(FeedPost::class).on(path(FeedPost::teamId).equal(path(FeedTeam::id))),
                        join(FeedPostTag::class).on(path(FeedPostTag::postId).equal(path(FeedPost::id))),
                    ).where(
                        path(FeedTeam::code).eq(teamCode),
                    )
                },
                Long::class.javaObjectType,
            ).resultList
            .map(Long::toLong)

    private fun readRelatedTagIds(seedTagIds: Collection<Long>): List<Long> =
        (
            readTargetTagIds(seedTagIds) +
                readSourceTagIds(seedTagIds)
            ).distinct()

    private fun readTargetTagIds(seedTagIds: Collection<Long>): List<Long> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectDistinct<Long>(
                        path(FeedTagRelation::targetTagId),
                    ).from(
                        entity(FeedTagRelation::class),
                    ).whereAnd(
                        path(FeedTagRelation::relationType).eq(FeedTagRelationType.CO_OCCURRENCE),
                        path(FeedTagRelation::sourceTagId).`in`(seedTagIds),
                    )
                },
                Long::class.javaObjectType,
            ).resultList
            .map(Long::toLong)

    private fun readSourceTagIds(seedTagIds: Collection<Long>): List<Long> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectDistinct<Long>(
                        path(FeedTagRelation::sourceTagId),
                    ).from(
                        entity(FeedTagRelation::class),
                    ).whereAnd(
                        path(FeedTagRelation::relationType).eq(FeedTagRelationType.CO_OCCURRENCE),
                        path(FeedTagRelation::targetTagId).`in`(seedTagIds),
                    )
                },
                Long::class.javaObjectType,
            ).resultList
            .map(Long::toLong)

    private fun readPostsByTagIds(
        tagIds: Collection<Long>,
        cursor: DailyPopularPostCursor?,
        limit: Int,
    ): List<DailyPopularPostQueryRow> {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))

        return jdslExecutor
            .createQuery(
                jpql {
                    val score = coalesce(path(FeedPostEngagementStatDaily::viewCount), 0L)

                    selectNew<DailyPopularPostQueryRow>(
                        path(FeedPost::id),
                        path(FeedPost::thumbnailImageUrl),
                        path(FeedPost::publicImageUrl),
                        path(FeedPost::imageAspectRatio),
                        score,
                        path(FeedPost::createdAt),
                    ).from(
                        entity(FeedPost::class),
                        join(FeedPostTag::class).on(path(FeedPostTag::postId).equal(path(FeedPost::id))),
                        leftJoin(FeedPostEngagementStatDaily::class).on(
                            and(
                                path(FeedPostEngagementStatDaily::postId).equal(path(FeedPost::id)),
                                path(FeedPostEngagementStatDaily::statDate).equal(today),
                            ),
                        ),
                    ).whereAnd(
                        path(FeedPost::thumbnailStatus).eq(FeedThumbnailStatus.SUCCESS),
                        path(FeedPostTag::tagId).`in`(tagIds),
                        cursor?.let {
                            or(
                                score.lt(it.score),
                                and(
                                    score.eq(it.score),
                                    path(FeedPost::createdAt).lt(it.createdAt),
                                ),
                                and(
                                    score.eq(it.score),
                                    path(FeedPost::createdAt).eq(it.createdAt),
                                    path(FeedPost::id).lt(it.postId),
                                ),
                            )
                        },
                    ).groupBy(
                        path(FeedPost::id),
                        path(FeedPost::thumbnailImageUrl),
                        path(FeedPost::publicImageUrl),
                        path(FeedPost::imageAspectRatio),
                        path(FeedPost::createdAt),
                        path(FeedPostEngagementStatDaily::viewCount),
                    ).orderBy(
                        score.desc(),
                        path(FeedPost::createdAt).desc(),
                        path(FeedPost::id).desc(),
                    )
                },
                DailyPopularPostQueryRow::class.java,
                limit = limit,
            ).resultList
    }
}
