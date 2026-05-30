package server.tag.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.tag.domain.Tag
import server.tag.infrastructure.TagRepository

@Component
class TagWriter(
    private val tagRepository: TagRepository,
) {
    @Transactional
    fun writeAll(tags: Collection<Tag>): List<Tag> {
        if (tags.isEmpty()) {
            return emptyList()
        }

        return tagRepository.saveAll(tags)
    }
}
