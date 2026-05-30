package server.messaging.annotation

import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@EventHandler(
    consumerGroup = "",
    transaction = true,
)
annotation class TransactionEventHandler(
    @get:AliasFor(annotation = EventHandler::class, attribute = "consumerGroup")
    val consumerGroup: String,
)
