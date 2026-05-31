package server.lock

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class KeyedLockTest {
    private val redissonClient = mockk<RedissonClient>()
    private val lock = mockk<RLock>()
    private val keyedLock = KeyedLock(redissonClient)

    init {
        every { redissonClient.getLock("lock:post:1") } returns lock
    }

    @Test
    fun `withLock 은 락을 획득한 뒤 블록을 실행하고 해제한다`() {
        every { lock.tryLock(5_000L, TimeUnit.MILLISECONDS) } returns true
        every { lock.isHeldByCurrentThread } returns true
        every { lock.unlock() } just runs

        val result =
            keyedLock.withLock("lock:post:1") {
                "done"
            }

        result shouldBe "done"
        verify(exactly = 1) { lock.tryLock(5_000L, TimeUnit.MILLISECONDS) }
        verify(exactly = 1) { lock.unlock() }
    }

    @Test
    fun `withLock 은 지정한 대기 시간으로 락 획득을 시도한다`() {
        every { lock.tryLock(2_000L, TimeUnit.MILLISECONDS) } returns true
        every { lock.isHeldByCurrentThread } returns true
        every { lock.unlock() } just runs

        keyedLock.withLock("lock:post:1", Duration.ofSeconds(2)) {}

        verify(exactly = 1) { lock.tryLock(2_000L, TimeUnit.MILLISECONDS) }
    }

    @Test
    fun `withLock 은 락 획득에 실패하면 예외를 던진다`() {
        every { lock.tryLock(5_000L, TimeUnit.MILLISECONDS) } returns false
        val executed = AtomicBoolean(false)

        val exception =
            shouldThrow<LockException> {
                keyedLock.withLock("lock:post:1") {
                    executed.set(true)
                }
            }

        exception.message shouldBe "락을 획득하지 못했습니다: lock:post:1"
        executed.get() shouldBe false
        verify(exactly = 0) { lock.unlock() }
    }

    @Test
    fun `withLock 은 블록이 실패해도 락을 해제한다`() {
        every { lock.tryLock(5_000L, TimeUnit.MILLISECONDS) } returns true
        every { lock.isHeldByCurrentThread } returns true
        every { lock.unlock() } just runs

        shouldThrow<IllegalStateException> {
            keyedLock.withLock("lock:post:1") {
                error("boom")
            }
        }

        verify(exactly = 1) { lock.unlock() }
    }

    @Test
    fun `withLock 은 인터럽트가 발생하면 인터럽트 상태를 복원하고 예외를 던진다`() {
        every { lock.tryLock(5_000L, TimeUnit.MILLISECONDS) } throws InterruptedException("interrupted")

        try {
            val exception =
                shouldThrow<LockException> {
                    keyedLock.withLock("lock:post:1") {}
                }

            exception.message shouldBe "락 획득 대기 중 인터럽트가 발생했습니다: lock:post:1"
            Thread.currentThread().isInterrupted shouldBe true
        } finally {
            Thread.interrupted()
        }
    }
}
