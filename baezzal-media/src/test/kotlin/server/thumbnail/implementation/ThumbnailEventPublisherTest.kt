package server.thumbnail.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.messaging.EventPublisher
import server.thumbnail.applicaiton.ThumbnailUpdatedEvent

class ThumbnailEventPublisherTest {
    private val eventPublisher = mockk<EventPublisher>()
    private val thumbnailEventPublisher = ThumbnailEventPublisher(eventPublisher)

    @Test
    fun `thumbnail updated event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        every { eventPublisher.publish(capture(publishedEvent), any()) } returns Unit

        thumbnailEventPublisher.publishUploaded(
            postId = 1L,
            thumbnailUrl = "https://static.wowan.me/thumbnails/result.webp",
        )

        publishedEvent.captured shouldBe ThumbnailUpdatedEvent(
            postId = 1L,
            thumbnailUrl = "https://static.wowan.me/thumbnails/result.webp",
        )
    }
}
