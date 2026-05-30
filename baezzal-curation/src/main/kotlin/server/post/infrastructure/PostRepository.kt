package server.post.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.post.domain.Post

interface PostRepository : JpaRepository<Post, Long>
