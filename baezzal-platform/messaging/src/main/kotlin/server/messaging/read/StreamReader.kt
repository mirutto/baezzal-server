package server.messaging.read

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import server.messaging.StreamSubscription
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
internal class StreamReader(
    private val redis: StringRedisTemplate,
    private val messageProcessor: StreamMessageProcessor,
    private val streamGroupEnsurer: StreamGroupEnsurer,
) {
    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val jobs = ConcurrentHashMap<String, Job>()

    private data class ReadContext(
        val subscription: StreamSubscription,
        val ops: StreamOperations<String, String, String>,
        val consumer: Consumer,
        val options: StreamReadOptions,
    )

    @PostConstruct
    fun initialize() {
        messageProcessor.subscriptions().forEach { subscription ->
            streamGroupEnsurer.ensure(subscription)
            start(subscription)
        }
    }

    @PreDestroy
    fun clear() {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
        scope.cancel()
    }

    private fun start(subscription: StreamSubscription) {
        val jobKey = "${subscription.channel}::${subscription.consumerGroup}"
        jobs.computeIfAbsent(jobKey) {
            scope.launch { loop(subscription) }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loop(subscription: StreamSubscription) {
        val context = open(subscription)

        while (true) {
            try {
                val records = read(context)
                if (records.isEmpty()) continue
                messageProcessor.handleRecords(
                    subscription,
                    context.ops,
                    records,
                    scope,
                )
            } catch (exception: Exception) {
                if (isNoGroupException(exception)) {
                    streamGroupEnsurer.ensure(subscription)
                } else {
                    logger.warn(exception) {
                        "Read failed. channel=${subscription.channel} " +
                            "consumerGroup=${subscription.consumerGroup}"
                    }
                }
                Thread.sleep(500)
            }
        }
    }

    private fun open(subscription: StreamSubscription): ReadContext {
        val ops = redis.opsForStream<String, String>()
        val consumerName =
            "worker-${subscription.channel}-${subscription.consumerGroup}-${UUID.randomUUID()}"
        val consumer = Consumer.from(subscription.consumerGroup, consumerName)
        val options =
            StreamReadOptions
                .empty()
                .block(Duration.ofSeconds(1))
                .count(subscription.batchSize.toLong())

        return ReadContext(subscription, ops, consumer, options)
    }

    private fun read(context: ReadContext): List<MapRecord<String, String, String>> =
        context.ops.read(
            context.consumer,
            context.options,
            StreamOffset.create(
                context.subscription.channel,
                ReadOffset.lastConsumed(),
            ),
        ) ?: emptyList()

    private fun isNoGroupException(exception: Exception): Boolean =
        exception.message?.contains("NOGROUP", ignoreCase = true) == true
}
