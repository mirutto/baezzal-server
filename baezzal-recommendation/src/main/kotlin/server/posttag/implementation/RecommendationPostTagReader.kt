package server.posttag.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.posttag.domain.RecommendationPostTag
import server.posttag.infrastructure.RecommendationPostTagRepository

@Component
class RecommendationPostTagReader(
    private val recommendationPostTagRepository: RecommendationPostTagRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByPostId(postId: Long): List<RecommendationPostTag> =
        recommendationPostTagRepository.findAllByPostIdOrderByIdAsc(postId)
}
