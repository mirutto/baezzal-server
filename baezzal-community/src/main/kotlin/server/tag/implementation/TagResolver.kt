package server.tag.implementation

import org.springframework.stereotype.Component
import server.tag.domain.Tag

@Component
class TagResolver(
    private val tagReader: TagReader,
    private val tagWriter: TagWriter,
) {
    fun resolveAll(tagTitles: List<String>): List<Tag> {
        val normalizedTitles = normalizeTitles(tagTitles)
        if (normalizedTitles.isEmpty()) {
            return emptyList()
        }

        val existingTagsByTitle = tagReader.readAllByTitles(normalizedTitles).associateBy(Tag::title)
        val newTags = normalizedTitles
            .filterNot(existingTagsByTitle::containsKey)
            .map { title -> Tag(title = title) }
        val persistedNewTagsByTitle = tagWriter.writeAll(newTags).associateBy(Tag::title)

        return normalizedTitles.mapNotNull { title ->
            existingTagsByTitle[title] ?: persistedNewTagsByTitle[title]
        }
    }

    private fun normalizeTitles(tagTitles: List<String>): List<String> =
        tagTitles
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
}
