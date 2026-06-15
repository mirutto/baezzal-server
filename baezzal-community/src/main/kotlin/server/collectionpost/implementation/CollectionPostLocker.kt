package server.collectionpost.implementation

import global.lock.withConflictLock
import org.springframework.stereotype.Component
import server.lock.KeyedLock

@Component
class CollectionPostLocker(
    private val keyedLock: KeyedLock,
) {
    fun <T> withLock(
        collectionId: Long,
        postId: Long,
        action: () -> T,
    ): T = keyedLock.withConflictLock(key(collectionId, postId), action)

    private fun key(
        collectionId: Long,
        postId: Long,
    ): String = "lock:collection-post:$collectionId:$postId"
}
