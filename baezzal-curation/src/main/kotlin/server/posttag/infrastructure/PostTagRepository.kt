package server.posttag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.posttag.domain.PostTag

interface PostTagRepository : JpaRepository<PostTag, Long> {
    fun findAllByPostIdInOrderByCreatedAtAsc(postIds: Collection<Long>): List<PostTag>
}
