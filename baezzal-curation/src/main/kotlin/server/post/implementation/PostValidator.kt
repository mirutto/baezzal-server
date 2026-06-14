package server.post.implementation

import global.error.BadRequestException
import org.springframework.stereotype.Component
import server.post.infrastructure.PostImageUrlCache
import server.team.implementation.TeamReader

@Component
class PostValidator(
    private val teamReader: TeamReader,
    private val postImageUrlCache: PostImageUrlCache,
) {
    fun validateImageUrl(imageUrl: String) {
        if (imageUrl.isBlank()) {
            throw BadRequestException("imageUrl 은 비어 있을 수 없습니다")
        }

        if (!postImageUrlCache.isIssued(imageUrl)) {
            throw BadRequestException("post presigned url 로 생성된 imageUrl 만 사용할 수 있습니다")
        }
    }

    fun normalizeTeamId(teamId: Long?): Long? {
        val normalizedTeamId = teamId?.takeIf { it > 0 } ?: return null

        teamReader.readById(normalizedTeamId)

        return normalizedTeamId
    }
}
