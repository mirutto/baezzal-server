package server.feed.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.feed.query.FeedTagQuery
import server.feed.query.FeedTagRelationQuery
import server.feed.query.FeedTagSearchStatDailyQuery

@Service
class FeedTagService(
    private val feedTagSearchStatDailyQuery: FeedTagSearchStatDailyQuery,
    private val feedTagQuery: FeedTagQuery,
    private val feedTagRelationQuery: FeedTagRelationQuery,
) {
    @Transactional(readOnly = true)
    fun findDailyPopularTags(limit: Int?): List<DailyPopularTagData> {
        val normalizedLimit = normalizeLimit(limit)
        return feedTagSearchStatDailyQuery.readDailyPopularTags(normalizedLimit)
            .mapIndexed { index, row ->
                DailyPopularTagData(
                    rank = index + 1,
                    tagId = row.tagId,
                    title = row.title,
                    searchCount = row.searchCount,
                )
            }
    }

    @Transactional(readOnly = true)
    fun autocompleteTags(
        keyword: String,
        limit: Int?,
    ): List<TagAutocompleteData> {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank()) {
            return emptyList()
        }

        val normalizedLimit = normalizeLimit(limit)
        val matchedTags = feedTagQuery.readAutocompleteByKeyword(
            keyword = normalizedKeyword,
            limit = normalizedLimit,
        )
        val fallbackTags = if (matchedTags.isEmpty() || matchedTags.size >= normalizedLimit) {
            emptyList()
        } else {
            feedTagRelationQuery.readAutocompleteFallbackTags(
                seedTagIds = matchedTags.map { it.tagId },
                excludeTagIds = matchedTags.map { it.tagId },
                limit = normalizedLimit - matchedTags.size,
            )
        }

        return (matchedTags + fallbackTags).map {
            TagAutocompleteData(
                tagId = it.tagId,
                title = it.title,
            )
        }
    }

    private fun normalizeLimit(limit: Int?): Int = (limit ?: DEFAULT_LIMIT).coerceIn(1, 20)

    companion object {
        private const val DEFAULT_LIMIT = 10
    }
}
