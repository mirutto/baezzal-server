package server.set

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisSet(
    private val redis: StringRedisTemplate,
) {
    private val ops = redis.opsForSet()

    fun add(
        key: String,
        vararg values: String,
    ): Long = ops.add(key, *values) ?: 0L

    fun remove(
        key: String,
        vararg values: String,
    ): Long = ops.remove(key, *values) ?: 0L

    fun contains(
        key: String,
        value: String,
    ): Boolean = ops.isMember(key, value) ?: false

    fun members(key: String): Set<String> = ops.members(key) ?: emptySet()

    fun size(key: String): Long = ops.size(key) ?: 0L

    fun delete(key: String) {
        redis.delete(key)
    }
}
