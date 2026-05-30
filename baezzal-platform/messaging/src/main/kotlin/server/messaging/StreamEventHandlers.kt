package server.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import server.messaging.annotation.EventHandler

@Component
internal class StreamEventHandlers(
    private val context: ApplicationContext,
    private val beanFactory: ConfigurableListableBeanFactory,
    private val messagingProperties: MessagingProperties,
    txManager: PlatformTransactionManager?,
) {
    private data class Key(
        val channel: String,
        val consumerGroup: String,
        val type: String,
    )

    private val logger = KotlinLogging.logger {}
    private val transactionTemplate = txManager?.let(::TransactionTemplate)

    private val allHandlers = discoverAnnotatedHandlers().distinctBy(::toKey)
    private val handlerMap = allHandlers.associateBy(::toKey)

    fun find(
        subscription: StreamSubscription,
        type: String,
    ): StreamMessageHandler? =
        handlerMap[Key(subscription.channel, subscription.consumerGroup, type)]

    fun subscriptions(): List<StreamSubscription> = allHandlers.map { it.subscription }.distinct()

    private fun discoverAnnotatedHandlers(): List<StreamMessageHandler> =
        beanFactory.beanDefinitionNames
            .asSequence()
            .filterNot { it.startsWith("scopedTarget.") }
            .filter { beanFactory.isSingleton(it) }
            .mapNotNull { beanName ->
                val rawClass =
                    context.getType(beanName) ?: return@mapNotNull null
                beanName to ClassUtils.getUserClass(rawClass)
            }.filterNot { (_, targetClass) ->
                targetClass ==
                    StreamEventHandlers::class.java
            }.flatMap { (beanName, targetClass) ->
                resolveAnnotatedHandlers(beanName, targetClass).asSequence()
            }.toList()

    private fun resolveAnnotatedHandlers(
        beanName: String,
        targetClass: Class<*>,
    ): List<StreamMessageHandler> =
        targetClass.methods
            .asSequence()
            .mapNotNull { method ->
                val eventHandler =
                    AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        EventHandler::class.java,
                    )
                if (eventHandler != null && method.parameterCount != 1) {
                    logger.warn {
                        buildString {
                            append("Skip event handler method. ")
                            append("exactly one parameter is required: ")
                            append(targetClass.name)
                            append('.')
                            append(method.name)
                        }
                    }
                    return@mapNotNull null
                }

                eventHandler ?: return@mapNotNull null

                toHandler(
                    beanName = beanName,
                    method = method,
                    targetClass = targetClass,
                    payloadClass = method.parameterTypes[0],
                    subscription =
                        StreamSubscription(
                            channel = messagingProperties.defaultChannel,
                            consumerGroup = eventHandler.consumerGroup,
                        ),
                    transactional = eventHandler.transaction,
                )
            }.toList()

    private fun toHandler(
        beanName: String,
        method: java.lang.reflect.Method,
        targetClass: Class<*>,
        payloadClass: Class<out Any>,
        subscription: StreamSubscription,
        transactional: Boolean,
    ): StreamMessageHandler? {
        val skipReason =
            when {
                payloadClass == Any::class.java ->
                    buildString {
                        append("Skip event handler method. ")
                        append("concrete event parameter is required: ")
                        append(targetClass.name)
                        append('.')
                        append(method.name)
                    }
                subscription.consumerGroup.isBlank() ->
                    "Skip event handler method. consumerGroup is required: " +
                        "${targetClass.name}.${method.name}"
                else -> null
            }

        if (skipReason != null) {
            logger.warn {
                skipReason
            }
            return null
        }

        val handler: (Any) -> Unit = { payload ->
            val bean = context.getBean(beanName)
            if (!transactional) {
                invokeMethod(bean, method, payload)
            } else {
                val tx =
                    transactionTemplate ?: throw MessagingException(
                        "No PlatformTransactionManager for " +
                            "transactional event handler: " +
                            "${targetClass.name}.${method.name}",
                    )
                tx.executeWithoutResult { invokeMethod(bean, method, payload) }
            }
        }

        val type = payloadClass.simpleName ?: throw MessagingException("payload type name must not be null")
        return StreamMessageHandler(
            subscription = subscription,
            type = type,
            payloadClass = payloadClass,
            handler = handler,
        )
    }

    private fun toKey(handler: StreamMessageHandler): Key =
        Key(
            handler.subscription.channel,
            handler.subscription.consumerGroup,
            handler.type,
        )

    private fun invokeMethod(
        bean: Any,
        method: java.lang.reflect.Method,
        payload: Any,
    ) {
        val invocable = AopUtils.selectInvocableMethod(method, bean.javaClass)
        ReflectionUtils.makeAccessible(invocable)
        invocable.invoke(bean, payload)
    }
}
