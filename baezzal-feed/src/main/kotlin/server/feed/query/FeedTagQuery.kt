package server.feed.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.feed.model.tag.FeedTag

@Component
class FeedTagQuery(
    private val jdslExecutor: JdslExecutor,
) {
    @Transactional(readOnly = true)
    fun readAutocompleteByKeyword(
        keyword: String,
        limit: Int,
    ): List<TagAutocompleteQueryRow> =
        jdslExecutor
            .createQuery(
                jpql {
                    selectNew<TagAutocompleteQueryRow>(
                        path(FeedTag::id),
                        path(FeedTag::title),
                    ).from(
                        entity(FeedTag::class),
                    ).where(
                        path(FeedTag::title).like("%${keyword}%"),
                    ).orderBy(
                        path(FeedTag::title).asc(),
                        path(FeedTag::id).asc(),
                    )
                },
                TagAutocompleteQueryRow::class.java,
                limit = limit,
            ).resultList
}
