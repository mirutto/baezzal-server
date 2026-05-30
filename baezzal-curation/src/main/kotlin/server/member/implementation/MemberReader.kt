package server.member.implementation

import org.springframework.stereotype.Component
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.infrastructure.MemberRepository

@Component
class MemberReader(
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun readById(memberId: Long): Member? = memberRepository.findByIdOrNull(memberId)

    @Transactional(readOnly = true)
    fun readByProvider(
        provider: MemberProvider,
        providerKey: String
    ): Member? = memberRepository.findByProviderAndProviderKey(provider, providerKey)
}
