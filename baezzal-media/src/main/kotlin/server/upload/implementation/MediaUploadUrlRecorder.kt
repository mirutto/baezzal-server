package server.upload.implementation

import org.springframework.stereotype.Component
import server.cache.CacheMemory

@Component
class MediaUploadUrlRecorder(
    private val cacheMemory: CacheMemory,
) {
    fun recordIssued(
        prefix: String,
        fileUrl: String,
        expiresInSeconds: Int,
    ) {
        val key =
            when (prefix) {
                POST_IMAGE_PREFIX -> "post:image-url:$fileUrl"
                PROFILE_IMAGE_PREFIX -> "member:profile-image-url:$fileUrl"
                else -> return
            }

        cacheMemory.set(
            key = key,
            value = true,
            ttlMillis = expiresInSeconds * 1000L,
        )
    }

    companion object {
        private const val POST_IMAGE_PREFIX = "posts"
        private const val PROFILE_IMAGE_PREFIX = "profiles"
    }
}
