package global.auth

import server.member.domain.MemberRole

data class Passport(
    val memberId: Long,
    val role: MemberRole,
)
