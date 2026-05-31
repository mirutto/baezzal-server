package server.lock

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class KeyedLock(
    private val redissonClient: RedissonClient,
) {
    fun <T> withLock(
        key: String,
        action: () -> T,
    ): T = withLock(key, DEFAULT_WAIT_TIME, action)

    fun <T> withLock(
        key: String,
        waitTime: Duration,
        action: () -> T,
    ): T {
        require(key.isNotBlank()) { "key must not be blank" }

        val lock = redissonClient.getLock(key)
        val acquired =
            try {
                lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw LockException("락 획득 대기 중 인터럽트가 발생했습니다: $key", exception)
            }

        if (!acquired) {
            throw LockException("락을 획득하지 못했습니다: $key")
        }

        return try {
            action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    companion object {
        private val DEFAULT_WAIT_TIME: Duration = Duration.ofSeconds(5)
    }
}
