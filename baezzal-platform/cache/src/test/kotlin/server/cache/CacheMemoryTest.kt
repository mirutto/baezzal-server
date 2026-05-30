package server.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

class CacheMemoryTest {
    private val redis = mockk<StringRedisTemplate>()
    private val valueOps = mockk<ValueOperations<String, String>>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val cache = CacheMemory(redis, objectMapper)

    init {
        every { redis.opsForValue() } returns valueOps
    }

    @Test
    fun `ttlWithJitter 는 null 과 0 이하 값을 그대로 반환한다`() {
        ttlWithJitter(null).shouldBeNull()
        ttlWithJitter(0L) shouldBe 0L
        ttlWithJitter(-1L) shouldBe -1L
    }

    @Test
    fun `ttlWithJitter 는 양수 ttl 에 10 퍼센트 범위의 jitter 를 적용한다`() {
        repeat(100) {
            ttlWithJitter(1_000L).shouldNotBeNull() shouldBeInRange 900L..1_100L
        }
    }

    @Test
    fun `get 은 역직렬화에 성공하면 값을 반환한다`() {
        every { valueOps.get("member:1") } returns """{"id":1,"name":"baezzal"}"""

        val result = cache.get("member:1", CachedMember::class.java)

        result shouldBe CachedMember(id = 1, name = "baezzal")
    }

    @Test
    fun `get 은 역직렬화에 실패하면 null 을 반환한다`() {
        every { valueOps.get("member:1") } returns """{"id":"oops"}"""

        cache.get("member:1", CachedMember::class.java).shouldBeNull()
    }

    @Test
    fun `get generic 은 제네릭 타입 역직렬화에 성공하면 값을 반환한다`() {
        every { valueOps.get("members") } returns """[{"id":1,"name":"baezzal"}]"""

        val result = cache.get<List<CachedMember>>("members")

        result shouldBe listOf(CachedMember(id = 1, name = "baezzal"))
    }

    @Test
    fun `set 은 ttl 이 없으면 만료 시간 없이 저장한다`() {
        every { valueOps.set("member:1", """{"id":1,"name":"baezzal"}""") } returns Unit

        cache.set("member:1", CachedMember(id = 1, name = "baezzal"), null)

        verify(exactly = 1) {
            valueOps.set("member:1", """{"id":1,"name":"baezzal"}""")
        }
        verify(exactly = 0) {
            valueOps.set(any(), any(), any<Long>(), any<TimeUnit>())
        }
    }

    @Test
    fun `set generic 은 ttl 없이 저장한다`() {
        every { valueOps.set("member:1", """{"id":1,"name":"baezzal"}""") } returns Unit

        cache.set("member:1", CachedMember(id = 1, name = "baezzal"))

        verify(exactly = 1) {
            valueOps.set("member:1", """{"id":1,"name":"baezzal"}""")
        }
    }

    @Test
    fun `set 은 ttl 이 있으면 jitter 가 적용된 밀리초 만료 시간으로 저장한다`() {
        every {
            valueOps.set(
                "member:1",
                """{"id":1,"name":"baezzal"}""",
                any<Long>(),
                TimeUnit.MILLISECONDS,
            )
        } returns Unit

        cache.set("member:1", CachedMember(id = 1, name = "baezzal"), 1_000L)

        verify(exactly = 1) {
            valueOps.set(
                "member:1",
                """{"id":1,"name":"baezzal"}""",
                withArg { timeout ->
                    timeout shouldBeInRange 900L..1_100L
                },
                TimeUnit.MILLISECONDS,
            )
        }
    }

    @Test
    fun `mget 은 반환값이 부족해도 키 순서대로 매핑한다`() {
        every { valueOps.multiGet(listOf("a", "b", "c")) } returns listOf("A", null)

        val result = cache.mget(listOf("a", "b", "c"))

        result shouldContainExactly mapOf("a" to "A", "b" to null, "c" to null)
    }

    private data class CachedMember(
        val id: Int,
        val name: String,
    )
}
