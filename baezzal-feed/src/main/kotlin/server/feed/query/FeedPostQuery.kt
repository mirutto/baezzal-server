package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import global.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.FeedAuthorData
import server.feed.application.FeedMemberRowData
import server.feed.application.FeedPostData
import server.feed.application.FeedPostDetailData
import server.feed.application.FeedPostDetailRowData
import server.feed.application.FeedPostRowData
import server.feed.application.FeedTeamData
import server.feed.model.collection.FeedCollectionPost
import server.feed.model.member.FeedMember
import server.feed.model.post.FeedPost
import server.feed.model.post.FeedThumbnailStatus
import server.feed.model.posttag.FeedPostTag
import server.feed.model.tag.FeedTag
import server.feed.model.team.FeedTeam

@Component
class FeedPostQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readAll(): List<FeedPostData> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedPostRowData>(
                        path(FeedPost::id),
                        path(FeedPost::thumbnailImageUrl),
                        path(FeedPost::publicImageUrl),
                        path(FeedPost::imageAspectRatio),
                    ).from(
                        entity(FeedPost::class),
                    ).where(
                        path(FeedPost::thumbnailStatus).eq(FeedThumbnailStatus.SUCCESS),
                    ).orderBy(
                        path(FeedPost::createdAt).desc(),
                        path(FeedPost::id).desc(),
                    )
                },
                FeedPostRowData::class.java,
            ).resultList
            .map(FeedPostRowData::toFeedPostData)

    @Transactional(readOnly = true)
    fun readAllByMemberId(memberId: Long): List<FeedPostData> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedPostRowData>(
                        path(FeedPost::id),
                        path(FeedPost::thumbnailImageUrl),
                        path(FeedPost::publicImageUrl),
                        path(FeedPost::imageAspectRatio),
                    ).from(
                        entity(FeedPost::class),
                    ).whereAnd(
                        path(FeedPost::memberId).eq(memberId),
                        path(FeedPost::thumbnailStatus).eq(FeedThumbnailStatus.SUCCESS),
                    ).orderBy(
                        path(FeedPost::createdAt).desc(),
                        path(FeedPost::id).desc(),
                    )
                },
                FeedPostRowData::class.java,
            ).resultList
            .map(FeedPostRowData::toFeedPostData)

    @Transactional(readOnly = true)
    fun readAllByUsername(username: String): List<FeedPostData> {
        val memberId = readMemberIdByUsername(username)
        return readAllByMemberId(memberId)
    }

    @Transactional(readOnly = true)
    fun readDetail(postId: Long): FeedPostDetailData {
        val post = readPostDetailRow(postId)
        val member = readMemberRow(post.memberId)

        return FeedPostDetailData(
            postId = post.postId,
            viewCount = post.viewCount,
            rawImageUrl = post.rawImageUrl,
            publicImageUrl = post.publicImageUrl,
            imageAspectRatio = post.imageAspectRatio,
            status = post.status.name,
            author = FeedAuthorData(
                memberId = member.memberId,
                nickname = member.nickname,
                username = member.username,
                profileImage = member.profileImage,
                preferredTeam = member.preferredTeamId?.let(::readTeam),
            ),
            description = post.description,
            tagTitles = readTagTitles(postId),
            collectionPostCount = readCollectionPostCount(postId),
        )
    }

    private fun readTeam(teamId: Long): FeedTeamData? =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedTeamData>(
                        path(FeedTeam::code),
                        path(FeedTeam::name),
                    ).from(
                        entity(FeedTeam::class),
                    ).where(
                        path(FeedTeam::id).eq(teamId),
                    )
                },
                FeedTeamData::class.java,
            ).resultList
            .firstOrNull()

    private fun readPostDetailRow(postId: Long): FeedPostDetailRowData =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedPostDetailRowData>(
                        path(FeedPost::id),
                        path(FeedPost::memberId),
                        path(FeedPost::viewCount),
                        path(FeedPost::rawImageUrl),
                        path(FeedPost::publicImageUrl),
                        path(FeedPost::imageAspectRatio),
                        path(FeedPost::thumbnailStatus),
                        path(FeedPost::description),
                    ).from(
                        entity(FeedPost::class),
                    ).where(
                        path(FeedPost::id).eq(postId),
                    )
                },
                FeedPostDetailRowData::class.java,
            ).resultList
            .firstOrNull()
            ?: throw NotFoundException("게시글을 찾을 수 없습니다")

    private fun readMemberRow(memberId: Long): FeedMemberRowData =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedMemberRowData>(
                        path(FeedMember::id),
                        path(FeedMember::nickname),
                        path(FeedMember::username),
                        path(FeedMember::profileImage),
                        path(FeedMember::preferredTeamId),
                    ).from(
                        entity(FeedMember::class),
                    ).where(
                        path(FeedMember::id).eq(memberId),
                    )
                },
                FeedMemberRowData::class.java,
            ).resultList
            .firstOrNull()
            ?: throw NotFoundException("회원을 찾을 수 없습니다")

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

    private fun readTagTitles(postId: Long): List<String> =
        jdslExecutor
            .createQuery(
                jpql {
                    select(
                        path(FeedTag::title),
                    ).from(
                        entity(FeedPostTag::class),
                        join(FeedTag::class).on(path(FeedPostTag::tagId).equal(path(FeedTag::id))),
                    ).where(
                        path(FeedPostTag::postId).eq(postId),
                    ).orderBy(
                        path(FeedPostTag::id).asc(),
                    )
                },
                String::class.java,
            ).resultList

    private fun readCollectionPostCount(postId: Long): Long =
        jdslExecutor
            .createQuery(
                jpql {
                    select(
                        count(path(FeedCollectionPost::id)),
                    ).from(
                        entity(FeedCollectionPost::class),
                    ).where(
                        path(FeedCollectionPost::postId).eq(postId),
                    )
                },
                Long::class.javaObjectType,
            ).singleResult
            .toLong()
}
