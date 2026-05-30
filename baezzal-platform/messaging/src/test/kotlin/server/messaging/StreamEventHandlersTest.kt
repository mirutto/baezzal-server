package server.messaging

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.context.support.GenericApplicationContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import server.messaging.annotation.EventHandler
import server.messaging.annotation.TransactionEventHandler
import java.util.function.Supplier

class StreamEventHandlersTest {
    @Test
    fun `이벤트 핸들러는 메서드 파라미터 타입으로 등록된다`() {
        val context =
            GenericApplicationContext().apply {
                registerBean(
                    "sampleHandler",
                    SampleHandler::class.java,
                    Supplier {
                        SampleHandler()
                    },
                )
                refresh()
            }

        val handlers =
            StreamEventHandlers(
                context = context,
                beanFactory = context.beanFactory,
                messagingProperties =
                    MessagingProperties().apply {
                        defaultChannel =
                            "baezzal-default"
                    },
                txManager = null,
            )

        val subscription =
            StreamSubscription(
                channel = "baezzal-default",
                consumerGroup = "sample-group",
            )

        val handler =
            handlers.find(
                subscription,
                SampleEvent::class.simpleName!!,
            )

        handler?.payloadClass shouldBe SampleEvent::class.java
        handlers.subscriptions() shouldContain subscription
        context.close()
    }

    @Test
    fun `트랜잭션 이벤트 핸들러도 메서드 파라미터 타입으로 등록되고 실행된다`() {
        val context =
            GenericApplicationContext().apply {
                registerBean(
                    "transactionalHandler",
                    TransactionalHandler::class.java,
                    Supplier {
                        TransactionalHandler()
                    },
                )
                refresh()
            }

        val handlers =
            StreamEventHandlers(
                context = context,
                beanFactory = context.beanFactory,
                messagingProperties =
                    MessagingProperties().apply {
                        defaultChannel =
                            "baezzal-default"
                    },
                txManager = TestTransactionManager(),
            )

        val subscription =
            StreamSubscription(
                channel = "baezzal-default",
                consumerGroup = "tx-group",
            )
        val handler =
            requireNotNull(
                handlers.find(
                    subscription,
                    TransactionSampleEvent::class.simpleName!!,
                ),
            )
        val bean =
            context.getBean(
                "transactionalHandler",
                TransactionalHandler::class.java,
            )
        val event = TransactionSampleEvent("payload")

        handler.payloadClass shouldBe TransactionSampleEvent::class.java

        @Suppress("UNCHECKED_CAST")
        (handler.handler as (TransactionSampleEvent) -> Unit).invoke(event)

        bean.handledEvent shouldBe event
        context.close()
    }

    private data class SampleEvent(
        val value: String,
    )

    private data class TransactionSampleEvent(
        val value: String,
    )

    private class SampleHandler {
        @EventHandler(consumerGroup = "sample-group")
        fun handle(event: SampleEvent) {
            event.value.length
        }
    }

    private class TransactionalHandler {
        var handledEvent: TransactionSampleEvent? = null

        @TransactionEventHandler(consumerGroup = "tx-group")
        fun handle(event: TransactionSampleEvent) {
            handledEvent = event
        }
    }

    private class TestTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus =
            SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }
}
