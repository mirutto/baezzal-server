package server.outbox

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.messaging.EventPublisher

class OutboxEventDispatcherTest {
    @Test
    fun `미발행 이벤트가 없으면 아무 작업도 하지 않는다`() {
        val eventPublisher = mockk<EventPublisher>()
        val eventOutboxRepository = mockk<EventOutboxRepository>()
        val outboxEventMarker = mockk<OutboxEventMarker>(relaxed = true)
        every { eventOutboxRepository.findUnpublished(10) } returns emptyList()

        val dispatcher =
            OutboxEventDispatcher(
                eventPublisher,
                eventOutboxRepository,
                outboxEventMarker,
            )

        dispatcher.dispatchBatch(10)

        verify(exactly = 0) {
            eventPublisher.publish(any<String>(), any<String>(), any<String>())
        }
        verify(exactly = 0) { outboxEventMarker.markPublished(any()) }
    }

    @Test
    fun `미발행 이벤트가 있으면 발행 후 publishedAt 으로 마킹한다`() {
        val eventPublisher = mockk<EventPublisher>()
        val eventOutboxRepository = mockk<EventOutboxRepository>()
        val outboxEventMarker = mockk<OutboxEventMarker>(relaxed = true)
        val rows =
            listOf(
                EventOutbox(
                    id = 1L,
                    topic = "baezzal-default",
                    type = "type-1",
                    eventId = "e1",
                    payload = "payload-1",
                ),
                EventOutbox(
                    id = 2L,
                    topic = "baezzal-default",
                    type = "type-2",
                    eventId = "e2",
                    payload = "payload-2",
                ),
            )
        every { eventOutboxRepository.findUnpublished(5) } returns rows
        every {
            eventPublisher.publish(any<String>(), any<String>(), any<String>())
        } just runs

        val dispatcher =
            OutboxEventDispatcher(
                eventPublisher,
                eventOutboxRepository,
                outboxEventMarker,
            )

        dispatcher.dispatchBatch(5)

        verify(exactly = 1) {
            eventPublisher.publish(
                type = "type-1",
                payloadJson = "payload-1",
                eventId = "e1",
            )
        }
        verify(exactly = 1) {
            eventPublisher.publish(
                type = "type-2",
                payloadJson = "payload-2",
                eventId = "e2",
            )
        }
        verify(exactly = 1) { outboxEventMarker.markPublished(listOf(1L, 2L)) }
    }

    @Test
    fun `발행 실패한 이벤트는 마킹하지 않고 다음 이벤트를 처리한다`() {
        val eventPublisher = mockk<EventPublisher>()
        val eventOutboxRepository = mockk<EventOutboxRepository>()
        val outboxEventMarker = mockk<OutboxEventMarker>(relaxed = true)
        val rows =
            listOf(
                EventOutbox(
                    id = 1L,
                    topic = "baezzal-default",
                    type = "type-1",
                    eventId = "e1",
                    payload = "payload-1",
                ),
                EventOutbox(
                    id = 2L,
                    topic = "baezzal-default",
                    type = "type-2",
                    eventId = "e2",
                    payload = "payload-2",
                ),
            )
        every { eventOutboxRepository.findUnpublished(2) } returns rows
        every {
            eventPublisher.publish(
                type = "type-1",
                payloadJson = "payload-1",
                eventId = "e1",
            )
        } throws RuntimeException("fail")
        every {
            eventPublisher.publish(
                type = "type-2",
                payloadJson = "payload-2",
                eventId = "e2",
            )
        } just runs

        val dispatcher =
            OutboxEventDispatcher(
                eventPublisher,
                eventOutboxRepository,
                outboxEventMarker,
            )

        dispatcher.dispatchBatch(2)

        verify(exactly = 1) {
            eventPublisher.publish(
                type = "type-1",
                payloadJson = "payload-1",
                eventId = "e1",
            )
        }
        verify(exactly = 1) {
            eventPublisher.publish(
                type = "type-2",
                payloadJson = "payload-2",
                eventId = "e2",
            )
        }
        verify(exactly = 1) { outboxEventMarker.markPublished(listOf(2L)) }
    }
}
