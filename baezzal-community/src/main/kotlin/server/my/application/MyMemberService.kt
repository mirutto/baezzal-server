package server.my.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.implementation.MemberCachedReader
import server.team.implementation.TeamReader

@Service
class MyMemberService(
    private val memberCachedReader: MemberCachedReader,
    private val teamReader: TeamReader,
) {
    @Transactional(readOnly = true)
    fun getMyProfile(memberId: Long): MyMemberResult =
        memberCachedReader.readById(memberId).let { member ->
            MyMemberResult(
                member = member,
                preferredTeamCode = teamReader.resolveCode(member.preferredTeamId),
            )
        }
}
