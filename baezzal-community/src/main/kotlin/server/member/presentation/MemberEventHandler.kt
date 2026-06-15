package server.member.presentation

import org.springframework.stereotype.Component
import server.member.application.MemberService
import server.member.application.MemberUpdatedEvent
import server.messaging.annotation.EventHandler

@Component
class MemberEventHandler(
    private val memberService: MemberService,
) {
    @EventHandler("update-member")
    fun handleUpdated(event: MemberUpdatedEvent) {
        memberService.handleUpdated(event)
    }
}
