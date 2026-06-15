package server.post.implementation

import global.lock.withConflictLock
import org.springframework.stereotype.Component
import server.lock.KeyedLock

@Component
class PostBatchLocker(
    private val keyedLock: KeyedLock,
) {
    fun <T> withLock(action: () -> T): T = keyedLock.withConflictLock(LOCK_KEY, action)

    companion object {
        private const val LOCK_KEY = "lock:post:view-count-batch"
    }
}
