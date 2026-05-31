package server.post.implementation

import global.error.BadRequestException
import global.error.NotFoundException
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

    fun validateImageContentType(contentType: String) {
        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }
    }

    fun normalizeTeamId(teamId: Long?): Long? {
        val normalizedTeamId = teamId?.takeIf { it > 0 } ?: return null

        teamReader.readById(normalizedTeamId)
            ?: throw NotFoundException("팀을 찾을 수 없습니다")

        return normalizedTeamId
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
    }
}
