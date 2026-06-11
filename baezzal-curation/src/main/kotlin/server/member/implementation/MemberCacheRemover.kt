package server.member.implementation

import org.springframework.stereotype.Component
import server.member.infrastructure.MemberCache

@Component
class MemberCacheRemover(
    private val memberCache: MemberCache,
) {
    fun remove(
        memberId: Long,
        username: String,
    ) {
        memberCache.evict(memberId, username)
    }
}
