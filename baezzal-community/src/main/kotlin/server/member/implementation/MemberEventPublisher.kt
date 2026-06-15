package server.member.implementation

import org.springframework.stereotype.Component
import server.member.application.MemberCreatedEvent
import server.member.application.MemberUpdatedEvent
import server.member.domain.Member
import server.outbox.TransactionalEventPublisher

@Component
class MemberEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {
    fun publishCreated(memberId: Long) {
        transactionalEventPublisher.publish(
            MemberCreatedEvent(
                memberId = memberId,
            ),
        )
    }

    fun publishUpdated(member: Member) {
        transactionalEventPublisher.publish(
            MemberUpdatedEvent(
                memberId = member.id,
                username = member.username,
            ),
        )
    }
}
