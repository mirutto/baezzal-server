package server.cache

import com.fasterxml.jackson.core.type.TypeReference

inline fun <reified T> RedisCache.get(key: String): T? = get(key, object : TypeReference<T>() {})

fun <T> RedisCache.set(
    key: String,
    value: T,
) {
    set(key, value, null)
}

fun <T> RedisCache.setIfAbsent(
    key: String,
    value: T,
): Boolean = setIfAbsent(key, value, null)
