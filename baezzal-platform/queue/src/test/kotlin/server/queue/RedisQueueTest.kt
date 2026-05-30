package server.queue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.StringRedisTemplate

class RedisQueueTest {
    private val redis = mockk<StringRedisTemplate>()
    private val ops = mockk<ListOperations<String, String>>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val queue by lazy { RedisQueue(redis, objectMapper) }

    init {
        every { redis.opsForList() } returns ops
    }

    @Test
    fun `lPop generic 은 제네릭 타입 역직렬화에 성공하면 값을 반환한다`() {
        every { ops.leftPop("queue") } returns """[{"id":1,"name":"baezzal"}]"""

        val result = queue.lPop<List<CachedMember>>("queue")

        result shouldBe listOf(CachedMember(id = 1, name = "baezzal"))
    }

    private data class CachedMember(
        val id: Int,
        val name: String,
    )
}
