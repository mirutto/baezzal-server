package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.FeedTeamSummaryData
import server.feed.model.post.FeedPost
import server.feed.model.post.FeedThumbnailStatus
import server.feed.model.team.FeedTeam

@Component
class FeedTeamQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readTeams(): List<FeedTeamSummaryData> {
        val teams = readTeamSummaries()
        val teamIds = teams.map(FeedTeamSummaryQueryRow::teamId)
        val postCounts = readTeamPostCounts(teamIds)
        val thumbnailUrls = readTeamThumbnailUrls(teamIds)

        return teams.map { team ->
            FeedTeamSummaryData(
                teamCode = team.teamCode,
                name = team.name,
                postCount = postCounts[team.teamId] ?: 0L,
                thumbnailUrls = thumbnailUrls[team.teamId].orEmpty(),
            )
        }
    }

    private fun readTeamSummaries(): List<FeedTeamSummaryQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedTeamSummaryQueryRow>(
                        path(FeedTeam::id),
                        path(FeedTeam::code),
                        path(FeedTeam::name),
                    ).from(
                        entity(FeedTeam::class),
                    ).orderBy(
                        path(FeedTeam::sortOrder).asc(),
                    )
                },
                FeedTeamSummaryQueryRow::class.java,
            ).resultList

    private fun readTeamPostCounts(teamIds: List<Long>): Map<Long, Long> {
        if (teamIds.isEmpty()) {
            return emptyMap()
        }

        return jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedTeamPostCountQueryRow>(
                        path(FeedPost::teamId),
                        count(path(FeedPost::id)),
                    ).from(
                        entity(FeedPost::class),
                    ).where(
                        path(FeedPost::teamId).`in`(teamIds),
                    ).groupBy(
                        path(FeedPost::teamId),
                    )
                },
                FeedTeamPostCountQueryRow::class.java,
            ).resultList
            .associate { row -> row.teamId to row.postCount }
    }

    private fun readTeamThumbnailUrls(teamIds: List<Long>): Map<Long, List<String>> {
        if (teamIds.isEmpty()) {
            return emptyMap()
        }

        return jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedTeamThumbnailQueryRow>(
                        path(FeedPost::teamId),
                        path(FeedPost::thumbnailImageUrl),
                    ).from(
                        entity(FeedPost::class),
                    ).whereAnd(
                        path(FeedPost::teamId).`in`(teamIds),
                        path(FeedPost::thumbnailStatus).eq(FeedThumbnailStatus.SUCCESS),
                        path(FeedPost::thumbnailImageUrl).notEqual(""),
                    ).orderBy(
                        path(FeedPost::teamId).asc(),
                        path(FeedPost::createdAt).desc(),
                        path(FeedPost::id).desc(),
                    )
                },
                FeedTeamThumbnailQueryRow::class.java,
            ).resultList
            .groupBy(
                keySelector = FeedTeamThumbnailQueryRow::teamId,
                valueTransform = FeedTeamThumbnailQueryRow::thumbnailUrl,
            ).mapValues { (_, urls) -> urls.take(TEAM_THUMBNAIL_LIMIT) }
    }

    companion object {
        private const val TEAM_THUMBNAIL_LIMIT = 3
    }
}
