package server.post.implementation

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.post.infrastructure.PostImageUrlCache
import server.team.implementation.TeamReader

class PostValidatorTest {
    private val teamReader = mockk<TeamReader>()
    private val postImageUrlCache = mockk<PostImageUrlCache>()
    private val postValidator = PostValidator(teamReader, postImageUrlCache)

    @Test
    fun `post presigned url 로 생성되지 않은 image url 이면 예외가 발생한다`() {
        every { postImageUrlCache.isIssued("https://cdn.example.com/post.png") } returns false

        shouldThrow<BadRequestException> {
            postValidator.validateImageUrl("https://cdn.example.com/post.png")
        }
    }

    @Test
    fun `post presigned url 로 생성된 image url 이면 통과한다`() {
        every { postImageUrlCache.isIssued("https://cdn.example.com/post.png") } returns true

        postValidator.validateImageUrl("https://cdn.example.com/post.png")
    }
}
