package server.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisTakeCache(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val valueOps get() = redis.opsForValue()

    fun <T> take(
        key: String,
        type: Class<T>,
    ): T? {
        val json = valueOps.getAndDelete(key) ?: return null
        return runCatching { objectMapper.readValue(json, type) }.getOrNull()
    }

    fun <T> take(
        key: String,
        typeRef: TypeReference<T>,
    ): T? {
        val json = valueOps.getAndDelete(key) ?: return null
        return runCatching { objectMapper.readValue(json, typeRef) }.getOrNull()
    }
}
