package server.collection.implementation

import global.lock.withConflictLock
import org.springframework.stereotype.Component
import server.lock.KeyedLock

@Component
class CollectionLocker(
    private val keyedLock: KeyedLock,
) {
    fun <T> withLock(
        collectionId: Long,
        action: () -> T,
    ): T = keyedLock.withConflictLock("lock:collection:$collectionId", action)
}
