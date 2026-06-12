package server.my.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.implementation.MemberCachedReader

@Service
class MyMemberService(
    private val memberCachedReader: MemberCachedReader,
) {
    @Transactional(readOnly = true)
    fun getMyProfile(memberId: Long): MyMemberResult =
        MyMemberResult(memberCachedReader.readById(memberId))
}
