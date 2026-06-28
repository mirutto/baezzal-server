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
    fun readAllByPostId(postId: Long): List<PostTag> =
        postTagRepository.findAllByPostIdOrderByIdAsc(postId)
}
