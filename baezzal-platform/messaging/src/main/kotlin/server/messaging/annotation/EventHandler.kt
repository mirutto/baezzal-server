package server.messaging.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    val consumerGroup: String,
    val transaction: Boolean = false,
)
