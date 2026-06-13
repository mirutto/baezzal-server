package server.collection.presentation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.collection.application.CollectionService
import server.collection.domain.Collection
import server.member.application.MemberCreatedEvent

class CollectionEventHandlerTest {
    private val collectionService = mockk<CollectionService>()
    private val collectionEventHandler = CollectionEventHandler(collectionService)

    @Test
    fun `member created event 를 기본 collection 생성으로 위임한다`() {
        val event = MemberCreatedEvent(memberId = 7L)
        every { collectionService.createDefault(7L) } returns Collection(
            memberId = 7L,
            name = "",
            imageVersions = global.image.ImageVersions(),
        )

        collectionEventHandler.createDefaultCollection(event)

        verify(exactly = 1) { collectionService.createDefault(7L) }
    }
}
