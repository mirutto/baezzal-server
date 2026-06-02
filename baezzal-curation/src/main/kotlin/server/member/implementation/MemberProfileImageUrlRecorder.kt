package server.member.implementation

import org.springframework.stereotype.Component
import server.member.infrastructure.MemberProfileImageUrlCache

@Component
class MemberProfileImageUrlRecorder(
    private val memberProfileImageUrlCache: MemberProfileImageUrlCache,
) {
    fun record(
        imageUrl: String,
        expiresInSeconds: Int,
    ) {
        memberProfileImageUrlCache.setIssued(
            imageUrl = imageUrl,
            ttlMillis = expiresInSeconds * 1000L,
        )
    }
}
