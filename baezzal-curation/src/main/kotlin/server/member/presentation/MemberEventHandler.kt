package server.member.presentation

import org.springframework.stereotype.Component
import server.member.application.MediaUploadUrlIssuedEvent
import server.member.application.MemberService
import server.messaging.annotation.EventHandler

@Component
class MemberEventHandler(
    private val memberService: MemberService,
) {
    @EventHandler("record-member-profile-image-url")
    fun recordIssuedProfileImageUrl(event: MediaUploadUrlIssuedEvent) {
        memberService.recordIssuedProfileImageUrl(event)
    }
}
