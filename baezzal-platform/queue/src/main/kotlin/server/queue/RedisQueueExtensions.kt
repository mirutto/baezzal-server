package server.queue

import com.fasterxml.jackson.core.type.TypeReference

inline fun <reified T> RedisQueue.lPop(key: String): T? = lPop(key, object : TypeReference<T>() {})

inline fun <reified T> RedisQueue.rPop(key: String): T? = rPop(key, object : TypeReference<T>() {})

inline fun <reified T> RedisQueue.drain(
    key: String,
    max: Int,
): List<T> = drain(key, object : TypeReference<T>() {}, max)
