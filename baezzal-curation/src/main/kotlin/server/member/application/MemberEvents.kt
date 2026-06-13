package server.member.application

data class MemberCreatedEvent(
    val memberId: Long
)

data class MemberUpdatedEvent(
    val memberId: Long,
    val username: String,
)
