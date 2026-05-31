package server.follow.implementation

import org.springframework.stereotype.Component
import server.lock.KeyedLock

@Component
class FollowLocker(
    private val keyedLock: KeyedLock,
) {
    fun <T> withLock(
        followerId: Long,
        followeeId: Long,
        action: () -> T,
    ): T = keyedLock.withLock(key(followerId, followeeId), action)

    private fun key(
        followerId: Long,
        followeeId: Long,
    ): String = "lock:follow:$followerId:$followeeId"
}
