package server.feed.implementation

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import global.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.application.FeedAuthorData
import server.feed.application.FeedImageData
import server.feed.application.FeedMemberRowData
import server.feed.application.FeedPostData
import server.feed.application.FeedPostDetailData
import server.feed.application.FeedPostDetailRowData
import server.feed.application.FeedPostRowData
import server.feed.application.FeedTeamData
import server.feed.domain.FeedCollectionPost
import server.feed.domain.FeedMember
import server.feed.domain.FeedPost
import server.feed.domain.FeedPostTag
import server.feed.domain.FeedTag
import server.feed.domain.FeedTeam
import server.feed.infrastructure.JdslExecutor

@Component
class FeedReader(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readAll(): List<FeedPostData> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<FeedPostRowData>(
                        path(FeedPost::id),
                        path(FeedPost::viewCount),
                        path(FeedPost::imageUrl),
                        path(FeedPost::imageWidth),
                        path(FeedPost::imageHeight),
                        path(FeedPost::imageAspectRatio),
                        path(FeedPost::thumbnailUrl),
                        path(FeedPost::thumbnailWidth),
                        path(FeedPost::thumbnailHeight),
                        path(FeedPost::thumbnailAspectRatio),
                    ).from(
                        entity(FeedPost::class),
                    ).orderBy(
                        path(FeedPost::createdAt).desc(),
                        path(FeedPost::id).desc(),
                    )
                },
                FeedPostRowData::class.java,
            ).resultList
            .map(FeedPostRowData::toFeedPostData)

    @Transactional(readOnly = true)
    fun readDetail(postId: Long): FeedPostDetailData {
        val post = readPostDetailRow(postId)
        val member = readMemberRow(post.memberId)

        return FeedPostDetailData(
            postId = post.postId,
            viewCount = post.viewCount,
            image = toFeedImageData(
                url = post.imageUrl,
                width = post.imageWidth,
                height = post.imageHeight,
                aspectRatio = post.imageAspectRatio,
            ),
            thumbnail = toFeedImageData(
                url = post.thumbnailUrl,
                width = post.thumbnailWidth,
                height = post.thumbnailHeight,
                aspectRatio = post.thumbnailAspectRatio,
            ),
            author = FeedAuthorData(
                memberId = member.memberId,
                nickname = member.nickname,
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
                        path(FeedTeam::id),
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
                        path(FeedPost::imageUrl),
                        path(FeedPost::imageWidth),
                        path(FeedPost::imageHeight),
                        path(FeedPost::imageAspectRatio),
                        path(FeedPost::thumbnailUrl),
                        path(FeedPost::thumbnailWidth),
                        path(FeedPost::thumbnailHeight),
                        path(FeedPost::thumbnailAspectRatio),
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

    private fun toFeedImageData(
        url: String,
        width: Int?,
        height: Int?,
        aspectRatio: Double?,
    ) = FeedImageData(
        url = url,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
    )
}
