package server.post.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.outbox.TransactionalEventPublisher
import server.post.domain.Post
import server.post.domain.PostCreatedEvent

class PostEventPublisherTest {
    private val transactionalEventPublisher = mockk<TransactionalEventPublisher>()
    private val postEventPublisher = PostEventPublisher(transactionalEventPublisher)

    @Test
    fun `post created event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        every { transactionalEventPublisher.publish(capture(publishedEvent)) } returns Unit

        postEventPublisher.publishCreated(
            Post(
                id = 1L,
                memberId = 1L,
                imageUrl = "https://cdn.example.com/post.png",
            ),
        )

        publishedEvent.captured shouldBe PostCreatedEvent(
            postId = 1L,
            imageUrl = "https://cdn.example.com/post.png",
        )
    }
}
