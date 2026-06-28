package server.posttag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.posttag.domain.PostTag

interface PostTagRepository : JpaRepository<PostTag, Long> {
    fun findAllByPostIdOrderByIdAsc(postId: Long): List<PostTag>
}
