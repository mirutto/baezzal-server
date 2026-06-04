package server.posttag.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.posttag.domain.PostTag
import server.posttag.infrastructure.PostTagRepository

@Component
class PostTagReader(
    private val postTagRepository: PostTagRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByPostId(postId: Long): List<PostTag> = readAllByPostIds(listOf(postId))

    @Transactional(readOnly = true)
    fun readAllByPostIds(postIds: Collection<Long>): List<PostTag> {
        if (postIds.isEmpty()) {
            return emptyList()
        }

        return postTagRepository.findAllByPostIdInOrderByCreatedAtAsc(postIds)
    }
}
