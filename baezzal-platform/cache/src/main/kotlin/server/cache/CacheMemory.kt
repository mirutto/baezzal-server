package server.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.types.Expiration
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class CacheMemory(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val valueOps get() = redis.opsForValue()

    fun <T> get(
        key: String,
        type: Class<T>,
    ): T? {
        val json = valueOps.get(key) ?: return null
        return runCatching { objectMapper.readValue(json, type) }.getOrNull()
    }

    fun <T> get(
        key: String,
        typeRef: TypeReference<T>,
    ): T? {
        val json = valueOps.get(key) ?: return null
        return runCatching { objectMapper.readValue(json, typeRef) }.getOrNull()
    }

    fun <T> set(
        key: String,
        value: T,
        ttlMillis: Long?,
    ) {
        val json = objectMapper.writeValueAsString(value)
        val adjustedTtlMillis = ttlWithJitter(ttlMillis)
        if (adjustedTtlMillis == null) {
            valueOps.set(key, json)
        } else {
            valueOps.set(key, json, adjustedTtlMillis, TimeUnit.MILLISECONDS)
        }
    }

    fun <T> setIfAbsent(
        key: String,
        value: T,
        ttlMillis: Long?,
    ): Boolean {
        val json = objectMapper.writeValueAsString(value)
        val adjustedTtlMillis = ttlWithJitter(ttlMillis)
        return if (adjustedTtlMillis ==
            null
        ) {
            valueOps.setIfAbsent(key, json) ?: false
        } else {
            valueOps.setIfAbsent(
                key,
                json,
                adjustedTtlMillis,
                TimeUnit.MILLISECONDS,
            )
                ?: false
        }
    }

    fun incr(key: String): Long = valueOps.increment(key) ?: 0L

    fun decrBy(
        key: String,
        delta: Long,
    ): Long = valueOps.increment(key, -delta) ?: 0L

    fun evict(key: String) = redis.delete(key)

    fun evictByPrefix(prefix: String) {
        val pattern = "$prefix*"
        val scanOptions =
            ScanOptions
                .scanOptions()
                .match(
                    pattern,
                ).count(500)
                .build()
        redis.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                redis.delete(cursor.next())
            }
        }
    }

    fun mget(keys: Collection<String>): Map<String, String?> {
        if (keys.isEmpty()) return emptyMap()
        val keyList = keys.toList()
        val values = valueOps.multiGet(keyList) ?: emptyList()
        return keyList
            .mapIndexed {
                idx,
                key,
                ->
                key to values.getOrNull(idx)
            }.toMap()
    }

    fun <T> mgetAs(
        keys: Collection<String>,
        typeRef: TypeReference<T>,
    ): Map<String, T?> {
        val raw = mget(keys)
        if (raw.isEmpty()) return emptyMap()
        return raw.mapValues { (_, json) ->
            if (json ==
                null
            ) {
                null
            } else {
                runCatching {
                    objectMapper.readValue(
                        json,
                        typeRef,
                    )
                }.getOrNull()
            }
        }
    }

    fun mset(
        valuesByKey: Map<String, Any>,
        ttlMillis: Long?,
    ) {
        if (valuesByKey.isEmpty()) return
        val jsonByKey =
            valuesByKey.mapValues { (_, v) ->
                objectMapper.writeValueAsString(v)
            }
        valueOps.multiSet(jsonByKey)
        if (ttlMillis != null) {
            for (key in jsonByKey.keys) {
                val expiration =
                    Expiration.milliseconds(
                        ttlWithJitter(ttlMillis) ?: ttlMillis,
                    )
                redis.expire(
                    key,
                    expiration.expirationTimeInMilliseconds,
                    TimeUnit.MILLISECONDS,
                )
            }
        }
    }
}
