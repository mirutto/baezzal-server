package server.posttag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.posttag.domain.RecommendationPostTag

interface RecommendationPostTagRepository : JpaRepository<RecommendationPostTag, Long> {
    fun findAllByPostIdOrderByIdAsc(postId: Long): List<RecommendationPostTag>
}
