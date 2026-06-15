package server.member.implementation

import global.image.ImageStatus
import global.image.ImageVersions
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.member.application.MemberCreatedEvent
import server.member.application.MemberUpdatedEvent
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.outbox.TransactionalEventPublisher

class MemberEventPublisherTest {
    private val transactionalEventPublisher = mockk<TransactionalEventPublisher>()
    private val memberEventPublisher = MemberEventPublisher(transactionalEventPublisher)

    @Test
    fun `member created event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        every { transactionalEventPublisher.publish(capture(publishedEvent)) } returns Unit

        memberEventPublisher.publishCreated(1L)

        publishedEvent.captured shouldBe MemberCreatedEvent(memberId = 1L)
    }

    @Test
    fun `member updated event 를 발행한다`() {
        val publishedEvent = slot<Any>()
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = ImageVersions(
                rawUrl = "https://example.com/profile.png",
                publicUrl = "https://example.com/profile.png",
                thumbnailUrl = "https://example.com/profile.png",
                status = ImageStatus.SUCCESS,
                aspectRatio = 1.0,
            ),
            description = "description",
            preferredTeamId = 3L,
            role = MemberRole.USER,
        )
        every { transactionalEventPublisher.publish(capture(publishedEvent)) } returns Unit

        memberEventPublisher.publishUpdated(member)

        publishedEvent.captured shouldBe MemberUpdatedEvent(
            memberId = 1L,
            username = "tester-username",
        )
    }
}
