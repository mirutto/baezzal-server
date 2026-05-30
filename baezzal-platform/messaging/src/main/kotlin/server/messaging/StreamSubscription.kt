package server.messaging

internal data class StreamSubscription(
    val channel: String,
    val consumerGroup: String,
    val ackOnFailure: Boolean = true,
    val processSequentially: Boolean = false,
    val batchSize: Int = 10,
)
