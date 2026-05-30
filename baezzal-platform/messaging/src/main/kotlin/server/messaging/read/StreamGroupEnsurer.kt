package server.messaging.read

import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import server.messaging.MessagingException
import server.messaging.StreamSubscription
import java.util.concurrent.ConcurrentHashMap

@Component
internal class StreamGroupEnsurer(
    private val redis: StringRedisTemplate,
) {
    private data class EnsureKey(
        val channel: String,
        val consumerGroup: String,
    )

    private val ensured = ConcurrentHashMap.newKeySet<EnsureKey>()

    fun ensure(subscription: StreamSubscription) {
        val ensureKey =
            EnsureKey(subscription.channel, subscription.consumerGroup)
        if (!ensured.add(ensureKey)) return

        try {
            redis.execute { connection ->
                val rawKey =
                    redis.stringSerializer.serialize(subscription.channel)
                        ?: throw MessagingException("channel serialization must not be null")
                connection
                    .streamCommands()
                    .xGroupCreate(
                        rawKey,
                        subscription.consumerGroup,
                        ReadOffset.from("0-0"),
                        true,
                    )
            }
        } catch (_: Exception) {
        }
    }
}
