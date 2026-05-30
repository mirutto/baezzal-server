package server.member.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.member.domain.Member
import server.member.domain.MemberProvider

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByProviderAndProviderKey(
        provider: MemberProvider,
        providerKey: String,
    ): Member?
}
