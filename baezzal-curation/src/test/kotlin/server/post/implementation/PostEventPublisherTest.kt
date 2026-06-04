package server.post.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.outbox.TransactionalEventPublisher
import server.post.application.PostCreatedEvent
import server.post.application.PostViewedEvent
import server.post.domain.ImageAsset
import server.post.domain.Post

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
                originalImage = ImageAsset(url = "https://cdn.example.com/post.png"),
            ),
        )

        publishedEvent.captured shouldBe PostCreatedEvent(
            postId = 1L,
            imageUrl = "https://cdn.example.com/post.png",
        )
    }

    @Test
    fun `post viewed event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        every { transactionalEventPublisher.publish(capture(publishedEvent)) } returns Unit

        postEventPublisher.publishViewed(
            userId = 3L,
            postId = 10L,
        )

        val event = publishedEvent.captured as PostViewedEvent
        event.userId shouldBe 3L
        event.postId shouldBe 10L
    }
}
