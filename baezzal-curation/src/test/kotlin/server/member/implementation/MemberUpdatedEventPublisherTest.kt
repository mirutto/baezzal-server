package server.member.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.member.application.MemberUpdatedEvent
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.outbox.TransactionalEventPublisher

class MemberUpdatedEventPublisherTest {
    private val transactionalEventPublisher = mockk<TransactionalEventPublisher>()
    private val memberUpdatedEventPublisher = MemberUpdatedEventPublisher(transactionalEventPublisher)

    @Test
    fun `member updated event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = "https://example.com/profile.png",
            description = "description",
            preferredTeamId = 3L,
            role = MemberRole.USER,
        )
        every { transactionalEventPublisher.publish(capture(publishedEvent)) } returns Unit

        memberUpdatedEventPublisher.publish(member)

        publishedEvent.captured shouldBe MemberUpdatedEvent(
            memberId = 1L,
            username = "tester-username",
        )
    }
}
