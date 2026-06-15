package server.member.implementation

import global.error.BadRequestException
import org.springframework.stereotype.Component
import server.member.infrastructure.MemberProfileImageUrlCache

@Component
class MemberProfileImageValidator(
    private val memberProfileImageUrlCache: MemberProfileImageUrlCache,
) {
    fun validateImageUrl(imageUrl: String) {
        if (imageUrl.isBlank()) {
            throw BadRequestException("profileImage 는 비어 있을 수 없습니다")
        }

        if (!memberProfileImageUrlCache.isIssued(imageUrl)) {
            throw BadRequestException("profileImage presigned url 로 생성된 imageUrl 만 사용할 수 있습니다")
        }
    }
}
