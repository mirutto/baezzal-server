package server.messaging.read

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.stereotype.Component
import server.messaging.MessageHandlerInvoker
import server.messaging.StreamEventHandlers
import server.messaging.StreamMessageHandler
import server.messaging.StreamSubscription

@Component
internal class StreamMessageProcessor(
    private val handlers: StreamEventHandlers,
    private val objectMapper: ObjectMapper,
    private val messageHandlerInvoker: MessageHandlerInvoker,
) {
    private val logger = KotlinLogging.logger {}

    fun subscriptions(): List<StreamSubscription> = handlers.subscriptions()

    fun handleRecords(
        subscription: StreamSubscription,
        ops: StreamOperations<String, String, String>,
        records: List<MapRecord<String, String, String>>,
        launchScope: CoroutineScope,
    ) {
        for (record in records) {
            handleRecord(subscription, ops, record, launchScope)
        }
    }

    private fun handleRecord(
        subscription: StreamSubscription,
        ops: StreamOperations<String, String, String>,
        record: MapRecord<String, String, String>,
        launchScope: CoroutineScope,
    ) {
        val type = record.value["type"]
        val payloadJson = record.value["payload"]
        val eventId = record.value["eventId"]

        when {
            type.isNullOrBlank() || payloadJson.isNullOrBlank() -> {
                ack(ops, subscription, record.id)
            }
            else -> {
                val messageHandler = handlers.find(subscription, type)
                if (messageHandler == null) {
                    logger.debug {
                        "No handler. channel=${subscription.channel} " +
                            "consumerGroup=${subscription.consumerGroup} type=$type"
                    }
                    ack(ops, subscription, record.id)
                    return
                }

                val payload =
                    deserializePayload(
                        ops = ops,
                        subscription = subscription,
                        record = record,
                        type = type,
                        payloadJson = payloadJson,
                        messageHandler = messageHandler,
                    ) ?: return

                if (subscription.processSequentially) {
                    handleSynchronously(
                        subscription = subscription,
                        ops = ops,
                        recordId = record.id,
                        eventId = eventId,
                        type = type,
                        payload = payload,
                        messageHandler = messageHandler,
                    )
                } else {
                    handleAsynchronously(
                        subscription = subscription,
                        ops = ops,
                        record = record,
                        eventId = eventId,
                        type = type,
                        payload = payload,
                        messageHandler = messageHandler,
                        launchScope = launchScope,
                    )
                }
            }
        }
    }

    private fun deserializePayload(
        ops: StreamOperations<String, String, String>,
        subscription: StreamSubscription,
        record: MapRecord<String, String, String>,
        type: String,
        payloadJson: String,
        messageHandler: StreamMessageHandler,
    ): Any? =
        runCatching {
            objectMapper.readValue(payloadJson, messageHandler.payloadClass)
        }.getOrElse { exception ->
            logger.warn(exception) {
                "Payload deserialize failed. channel=${subscription.channel} " +
                    "consumerGroup=${subscription.consumerGroup} type=$type id=${record.id}"
            }
            if (subscription.ackOnFailure) {
                ack(
                    ops = ops,
                    subscription = subscription,
                    recordId = record.id,
                )
            }
            null
        }

    @Suppress("TooGenericExceptionCaught")
    private fun handleAsynchronously(
        subscription: StreamSubscription,
        ops: StreamOperations<String, String, String>,
        record: MapRecord<String, String, String>,
        eventId: String?,
        type: String,
        payload: Any,
        messageHandler: StreamMessageHandler,
        launchScope: CoroutineScope,
    ) {
        launchScope.launch {
            try {
                messageHandlerInvoker.invoke(
                    eventId,
                    type,
                    payload,
                    messageHandler.handler,
                )
                ack(ops, subscription, record.id)
            } catch (exception: Exception) {
                logger.warn(exception) {
                    "Handler failed. channel=${subscription.channel} " +
                        "consumerGroup=${subscription.consumerGroup} type=$type id=${record.id}"
                }
                if (subscription.ackOnFailure) {
                    ack(ops, subscription, record.id)
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun handleSynchronously(
        subscription: StreamSubscription,
        ops: StreamOperations<String, String, String>,
        recordId: RecordId,
        eventId: String?,
        type: String,
        payload: Any,
        messageHandler: StreamMessageHandler,
    ) {
        try {
            messageHandlerInvoker.invoke(
                eventId,
                type,
                payload,
                messageHandler.handler,
            )
            ack(ops, subscription, recordId)
        } catch (exception: Exception) {
            logger.warn(exception) {
                "Handler failed. channel=${subscription.channel} " +
                    "consumerGroup=${subscription.consumerGroup} type=$type id=$recordId"
            }
            if (subscription.ackOnFailure) {
                ack(ops, subscription, recordId)
            }
        }
    }

    private fun ack(
        ops: StreamOperations<String, String, String>,
        subscription: StreamSubscription,
        recordId: RecordId,
    ) {
        ops.acknowledge(
            subscription.channel,
            subscription.consumerGroup,
            recordId,
        )
    }
}
