package server.post.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import server.post.implementation.PostImageUploader
import java.util.UUID

@Service
class PostService(
    private val postImageUploader: PostImageUploader,
) {
    fun createImageUploadUrl(command: CreatePostImageUploadUrlCommand): PostImageUploadUrlResult {
        val prefix = POST_IMAGE_PREFIX
        val fileName = UUID.randomUUID().toString()
        val contentType = command.contentType.trim().lowercase()
        validate(contentType)

        return PostImageUploadUrlResult.from(
            postImageUploader.createPresignedUploadUrl(
                prefix = prefix,
                fileName = fileName,
                contentType = contentType,
            ),
        )
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
        private const val POST_IMAGE_PREFIX = "posts"
    }

    private fun validate(contentType: String) {
        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }
    }
}
