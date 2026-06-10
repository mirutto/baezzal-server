package server.post.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.messaging.EventPublisher
import server.post.application.PostCreatedEvent
import server.post.domain.ImageAsset
import server.post.domain.Post

class PostEventPublisherTest {
    private val eventPublisher = mockk<EventPublisher>()
    private val postEventPublisher = PostEventPublisher(eventPublisher)

    @Test
    fun `post created event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        every { eventPublisher.publish(capture(publishedEvent), any()) } returns Unit

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
}
