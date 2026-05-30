package server.tag.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.tag.domain.Tag
import server.tag.infrastructure.TagRepository

@Component
class TagReader(
    private val tagRepository: TagRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByTitles(titles: Collection<String>): List<Tag> = tagRepository.findAllByTitleIn(titles)
}
