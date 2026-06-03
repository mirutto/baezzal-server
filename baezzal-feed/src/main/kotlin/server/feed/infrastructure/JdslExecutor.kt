package server.feed.infrastructure

import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQuery
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TypedQuery
import org.springframework.stereotype.Component

@Component
class JdslExecutor(
    @PersistenceContext
    private val entityManager: EntityManager,
) {
    private val renderer = JpqlRenderer()
    private val renderContext = JpqlRenderContext()

    fun <T : Any> createQuery(
        query: JpqlQuery<*>,
        resultClass: Class<T>,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE,
    ): TypedQuery<T> {
        val rendered = renderer.render(query, renderContext)
        val typedQuery = entityManager.createQuery(rendered.query, resultClass)

        rendered.params.forEach { (name, value) ->
            typedQuery.setParameter(name, value)
        }

        typedQuery.firstResult = offset
        typedQuery.maxResults = limit

        return typedQuery
    }
}
