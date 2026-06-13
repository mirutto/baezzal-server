package server.member.implementation

import org.springframework.stereotype.Component
import server.member.application.MemberUpdatedEvent
import server.member.domain.Member
import server.outbox.TransactionalEventPublisher

@Component
class MemberUpdatedEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {
    fun publish(member: Member) {
        transactionalEventPublisher.publish(
            MemberUpdatedEvent(
                memberId = member.id,
                username = member.username,
            ),
        )
    }
}
