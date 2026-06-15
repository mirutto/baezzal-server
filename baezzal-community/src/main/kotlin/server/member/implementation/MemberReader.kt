package server.member.implementation

import global.error.NotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.infrastructure.MemberRepository

@Component
class MemberReader(
    private val memberRepository: MemberRepository,
) {
    @Transactional(readOnly = true)
    fun readById(memberId: Long): Member = memberRepository.findByIdOrNull(memberId)
        ?: throw NotFoundException("회원을 찾을 수 없습니다")

    @Transactional(readOnly = true)
    fun readByUsername(username: String): Member = memberRepository.findByUsername(username)
        ?: throw NotFoundException("회원을 찾을 수 없습니다")

    @Transactional(readOnly = true)
    fun readByProvider(
        provider: MemberProvider,
        providerKey: String,
    ): Member? = memberRepository.findByProviderAndProviderKey(provider, providerKey)

    @Transactional(readOnly = true)
    fun readByIds(memberIds: List<Long>): List<Member> =
        memberRepository.findAllById(memberIds)
}
