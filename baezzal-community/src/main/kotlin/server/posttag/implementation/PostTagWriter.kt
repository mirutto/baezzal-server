package server.posttag.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.posttag.domain.PostTag
import server.posttag.infrastructure.PostTagRepository
import server.tag.domain.Tag

@Component
class PostTagWriter(
    private val postTagRepository: PostTagRepository,
) {
    @Transactional
    fun writeAll(postTags: Collection<PostTag>): List<PostTag> {
        if (postTags.isEmpty()) {
            return emptyList()
        }

        return postTagRepository.saveAll(postTags)
    }

    @Transactional
    fun writeAll(
        postId: Long,
        tags: List<Tag>,
    ): List<PostTag> = writeAll(
        tags.map { tag ->
            PostTag(
                postId = postId,
                tagId = tag.id,
            )
        },
    )
}
