package server.member.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.infrastructure.MemberCache

@Component
class MemberCachedReader(
    private val memberReader: MemberReader,
    private val memberCache: MemberCache,
) {
    @Transactional(readOnly = true)
    fun readById(memberId: Long): Member {
        memberCache.getById(memberId)?.let { return it }

        return memberReader.readById(memberId)
            .also(memberCache::set)
    }

    @Transactional(readOnly = true)
    fun readByUsername(username: String): Member {
        memberCache.getByUsername(username)?.let { return it }

        return memberReader.readByUsername(username)
            .also(memberCache::set)
    }
}
