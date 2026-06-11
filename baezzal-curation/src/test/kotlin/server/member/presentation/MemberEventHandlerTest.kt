package server.member.presentation

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.member.application.MemberService
import server.member.application.MemberUpdatedEvent

class MemberEventHandlerTest {
    private val memberService = mockk<MemberService>()
    private val memberEventHandler = MemberEventHandler(memberService)

    @Test
    fun `member updated event 를 member service 로 위임한다`() {
        val event = MemberUpdatedEvent(
            memberId = 1L,
            username = "tester-username",
        )
        every { memberService.handleUpdated(event) } returns Unit

        memberEventHandler.handleUpdated(event)

        verify(exactly = 1) { memberService.handleUpdated(event) }
    }
}
