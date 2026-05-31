package server.collection.implementation

import org.springframework.stereotype.Component
import server.lock.KeyedLock

@Component
class CollectionLocker(
    private val keyedLock: KeyedLock,
) {
    fun <T> withLock(
        collectionId: Long,
        action: () -> T,
    ): T = keyedLock.withLock("lock:collection:$collectionId", action)
}
