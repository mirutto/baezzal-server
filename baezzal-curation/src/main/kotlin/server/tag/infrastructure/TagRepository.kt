package server.tag.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.tag.domain.Tag

interface TagRepository : JpaRepository<Tag, Long> {
    fun findAllByTitleIn(titles: Collection<String>): List<Tag>
}
