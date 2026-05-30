package server.queue

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class QueueMemory(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val ops = redis.opsForList()

    fun rPush(
        key: String,
        value: Any,
    ): Long = ops.rightPush(key, objectMapper.writeValueAsString(value)) ?: 0L

    fun lPush(
        key: String,
        value: Any,
    ): Long = ops.leftPush(key, objectMapper.writeValueAsString(value)) ?: 0L

    fun rPushAll(
        key: String,
        values: Collection<Any>,
    ): Long {
        if (values.isEmpty()) return len(key)
        return ops.rightPushAll(
            key,
            values.map(objectMapper::writeValueAsString),
        )
            ?: 0L
    }

    fun lPushAll(
        key: String,
        values: Collection<Any>,
    ): Long {
        if (values.isEmpty()) return len(key)
        return ops.leftPushAll(
            key,
            values.map(objectMapper::writeValueAsString),
        )
            ?: 0L
    }

    fun <T> lPop(
        key: String,
        typeRef: TypeReference<T>,
    ): T? =
        ops.leftPop(key)?.let {
            runCatching { objectMapper.readValue(it, typeRef) }.getOrNull()
        }

    fun <T> rPop(
        key: String,
        typeRef: TypeReference<T>,
    ): T? =
        ops.rightPop(key)?.let {
            runCatching { objectMapper.readValue(it, typeRef) }.getOrNull()
        }

    fun <T> drain(
        key: String,
        typeRef: TypeReference<T>,
        max: Int,
    ): List<T> {
        val result = ArrayList<T>(minOf(max, 1000))
        repeat(max) {
            val value = lPop(key, typeRef) ?: return result
            result.add(value)
        }
        return result
    }

    fun len(key: String): Long = ops.size(key) ?: 0L

    fun delete(key: String) {
        redis.delete(key)
    }
}
