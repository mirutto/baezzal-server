package server.member.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.infrastructure.MemberRepository

@Component
class MemberWriter(
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun write(member: Member): Member = memberRepository.save(member)
}
