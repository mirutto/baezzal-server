package global.lock

import global.error.ConflictException
import server.lock.KeyedLock
import server.lock.LockException

fun <T> KeyedLock.withConflictLock(
    key: String,
    action: () -> T,
): T =
    try {
        withLock(key, action)
    } catch (_: LockException) {
        throw ConflictException("동시에 처리 중인 요청입니다. 잠시 후 다시 시도해주세요")
    }
