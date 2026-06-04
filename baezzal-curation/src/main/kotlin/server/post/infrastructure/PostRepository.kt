package server.post.infrastructure

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import server.post.domain.Post

interface PostRepository : JpaRepository<Post, Long> {
    fun findAllByOrderByCreatedAtDesc(): List<Post>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post post set post.viewCount = post.viewCount + :delta where post.id = :postId")
    fun increaseViewCount(
        @Param("postId") postId: Long,
        @Param("delta") delta: Long,
    ): Int
}
