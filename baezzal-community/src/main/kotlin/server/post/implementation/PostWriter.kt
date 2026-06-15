package server.post.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.post.domain.Post
import server.post.infrastructure.PostRepository

@Component
class PostWriter(
    private val postRepository: PostRepository,
) {
    @Transactional
    fun write(post: Post): Post = postRepository.save(post)

    @Transactional
    fun increaseViewCounts(viewCountByPostId: Map<Long, Long>) {
        viewCountByPostId.forEach { (postId, viewCount) ->
            postRepository.increaseViewCount(
                postId = postId,
                delta = viewCount,
            )
        }
    }
}
