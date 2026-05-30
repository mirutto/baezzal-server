package server.cache

import kotlin.math.roundToLong
import kotlin.random.Random

private const val TTL_JITTER_RATIO = 0.1

fun ttlWithJitter(ttlMillis: Long?): Long? {
    if (ttlMillis == null || ttlMillis <= 0) {
        return ttlMillis
    }

    val jitter = (ttlMillis * TTL_JITTER_RATIO).roundToLong()

    return when {
        jitter <= 0 -> ttlMillis
        else -> ttlMillis + Random.nextLong(from = -jitter, until = jitter + 1)
    }
}
