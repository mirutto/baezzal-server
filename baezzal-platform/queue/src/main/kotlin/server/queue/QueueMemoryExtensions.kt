package server.queue

import com.fasterxml.jackson.core.type.TypeReference

inline fun <reified T> QueueMemory.lPop(key: String): T? = lPop(key, object : TypeReference<T>() {})

inline fun <reified T> QueueMemory.rPop(key: String): T? = rPop(key, object : TypeReference<T>() {})

inline fun <reified T> QueueMemory.drain(
    key: String,
    max: Int,
): List<T> = drain(key, object : TypeReference<T>() {}, max)
