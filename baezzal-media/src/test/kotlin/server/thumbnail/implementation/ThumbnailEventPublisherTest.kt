package server.thumbnail.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.messaging.EventPublisher
import server.thumbnail.applicaiton.ImageAssetEvent
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
            originalImage = ImageAssetEvent(
                url = "https://cdn.example.com/post.png",
                width = 1280,
                height = 720,
                aspectRatio = 1280.0 / 720.0,
            ),
            thumbnailImage = ImageAssetEvent(
                url = "https://static.wowan.me/thumbnails/result.webp",
                width = 320,
                height = 180,
                aspectRatio = 320.0 / 180.0,
            ),
        )

        publishedEvent.captured shouldBe ThumbnailUpdatedEvent(
            postId = 1L,
            originalImage = ImageAssetEvent(
                url = "https://cdn.example.com/post.png",
                width = 1280,
                height = 720,
                aspectRatio = 1280.0 / 720.0,
            ),
            thumbnailImage = ImageAssetEvent(
                url = "https://static.wowan.me/thumbnails/result.webp",
                width = 320,
                height = 180,
                aspectRatio = 320.0 / 180.0,
            ),
        )
    }
}
