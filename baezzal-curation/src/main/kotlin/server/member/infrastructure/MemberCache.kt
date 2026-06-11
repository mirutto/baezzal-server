package server.member.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole

@Component
class MemberCache(
    private val cacheMemory: CacheMemory,
) {
    fun getById(memberId: Long): Member? =
        cacheMemory.get(
            key = keyById(memberId),
            type = MemberCacheData::class.java,
        )?.toMember()

    fun getByUsername(username: String): Member? =
        cacheMemory.get(
            key = keyByUsername(username),
            type = MemberCacheData::class.java,
        )?.toMember()

    fun set(member: Member) {
        val data = MemberCacheData(member)
        cacheMemory.set(keyById(member.id), data, null)
        cacheMemory.set(keyByUsername(member.username), data, null)
    }

    fun evict(
        memberId: Long,
        username: String,
    ) {
        cacheMemory.evict(keyById(memberId))
        cacheMemory.evict(keyByUsername(username))
    }

    private fun keyById(memberId: Long): String = "member:id:$memberId"

    private fun keyByUsername(username: String): String = "member:username:$username"

    internal data class MemberCacheData(
        val id: Long,
        val nickname: String,
        val username: String,
        val provider: MemberProvider,
        val providerKey: String,
        val profileImage: String,
        val description: String,
        val preferredTeamId: Long?,
        val role: MemberRole,
    ) {
        constructor(member: Member) : this(
            id = member.id,
            nickname = member.nickname,
            username = member.username,
            provider = member.provider,
            providerKey = member.providerKey,
            profileImage = member.profileImage,
            description = member.description,
            preferredTeamId = member.preferredTeamId,
            role = member.role,
        )

        fun toMember(): Member = Member(
            id = id,
            nickname = nickname,
            username = username,
            provider = provider,
            providerKey = providerKey,
            profileImage = profileImage,
            description = description,
            preferredTeamId = preferredTeamId,
            role = role,
        )
    }
}
