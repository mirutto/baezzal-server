package server.member.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.infrastructure.MemberRepository

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
) {
    @Transactional(readOnly = true)
    fun findSocialMember(
        provider: MemberProvider,
        providerKey: String,
    ): Member {
        return memberRepository.findByProviderAndProviderKey(
            provider = provider,
            providerKey = providerKey,
        ) ?: throw IllegalStateException(
            "Member not found. provider=$provider, providerKey=$providerKey",
        )
    }

    fun create(
        nickname: String,
        provider: MemberProvider,
        providerKey: String,
    ): Member {
        return memberRepository.save(
            Member(
                nickname = nickname,
                provider = provider,
                providerKey = providerKey,
            ),
        )
    }
}
