package server.post.application

import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.post.domain.Post
import server.post.implementation.PostEventPublisher
import server.post.implementation.PostImageUrlRecorder
import server.post.implementation.PostReader
import server.post.implementation.PostValidator
import server.post.implementation.PostWriter
import server.posttag.implementation.PostTagWriter
import server.tag.implementation.TagResolver

@Service
class PostService(
    private val postImageUrlRecorder: PostImageUrlRecorder,
    private val postValidator: PostValidator,
    private val postReader: PostReader,
    private val postWriter: PostWriter,
    private val postTagWriter: PostTagWriter,
    private val tagResolver: TagResolver,
    private val postEventPublisher: PostEventPublisher,
) {
    @Transactional
    fun create(
        memberId: Long,
        command: CreatePostCommand,
    ): CreatePostResult {
        val imageUrl = command.imageUrl.trim()
        val description = command.description.trim()

        postValidator.validateImageUrl(imageUrl)

        val post = postWriter.write(
            Post(
                memberId = memberId,
                imageUrl = imageUrl,
                description = description,
                teamId = postValidator.normalizeTeamId(command.teamId),
            ),
        )
        val tags = tagResolver.resolveAll(command.tagTitles)

        postTagWriter.writeAll(post.id, tags)

        postEventPublisher.publishCreated(post)

        return CreatePostResult(
            post = post,
            tags = tags,
        )
    }

    @Transactional
    fun updateThumbnail(postId: Long, thumbnailUrl: String) {
        val post = postReader.readById(postId)
            ?: throw NotFoundException("게시글을 찾을 수 없습니다")

        post.completeThumbnail(thumbnailUrl.trim())
    }

    fun recordIssuedImageUrl(event: MediaUploadUrlIssuedEvent) {
        if (event.prefix != POST_IMAGE_PREFIX) {
            return
        }

        postImageUrlRecorder.record(event.fileUrl, event.expiresInSeconds)
    }

    companion object {
        private const val POST_IMAGE_PREFIX = "posts"
    }
}
