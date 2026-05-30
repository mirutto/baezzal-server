package server.cache

import com.fasterxml.jackson.core.type.TypeReference

inline fun <reified T> CacheMemory.get(key: String): T? = get(key, object : TypeReference<T>() {})

fun <T> CacheMemory.set(
    key: String,
    value: T,
) {
    set(key, value, null)
}

fun <T> CacheMemory.setIfAbsent(
    key: String,
    value: T,
): Boolean = setIfAbsent(key, value, null)
