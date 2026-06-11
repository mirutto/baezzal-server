package server.member.application

data class MemberUpdatedEvent(
    val memberId: Long,
    val username: String,
)
