package server.messaging

internal data class StreamMessageHandler(
    val subscription: StreamSubscription,
    val type: String,
    val payloadClass: Class<out Any>,
    val handler: (Any) -> Unit,
)
