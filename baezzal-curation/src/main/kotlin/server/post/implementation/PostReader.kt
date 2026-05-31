package server.post.implementation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.post.domain.Post
import server.post.infrastructure.PostRepository

@Component
class PostReader(
    private val postRepository: PostRepository,
) {
    @Transactional(readOnly = true)
    fun readById(postId: Long): Post? = postRepository.findByIdOrNull(postId)
}
