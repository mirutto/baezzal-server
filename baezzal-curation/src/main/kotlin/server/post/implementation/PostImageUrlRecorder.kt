package server.post.implementation

import org.springframework.stereotype.Component
import server.post.infrastructure.PostImageUrlCache

@Component
class PostImageUrlRecorder(
    private val postImageUrlCache: PostImageUrlCache,
) {
    fun record(
        imageUrl: String,
        expiresInSeconds: Int,
    ) {
        postImageUrlCache.setIssued(
            imageUrl = imageUrl,
            ttlMillis = expiresInSeconds * 1000L,
        )
    }
}
