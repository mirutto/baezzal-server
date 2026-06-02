package server.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PushNotifierTest {
    private val firebaseMessaging = mockk<FirebaseMessaging>()
    private val pushNotifier = PushNotifier(firebaseMessaging = firebaseMessaging)

    @Test
    fun `푸시 메시지를 FCM 메시지로 변환해 전송한다`() {
        every { firebaseMessaging.send(any()) } returns "message-id"

        val result =
            pushNotifier.send(
                PushMessage(
                    token = "device-token",
                    title = "title",
                    body = "body",
                    data = mapOf("postId" to "1"),
                    imageUrl = "https://example.com/image.png",
                ),
            )

        result shouldBe "message-id"
        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `FCM 예외는 그대로 전달한다`() {
        val exception = mockk<FirebaseMessagingException>()
        every { firebaseMessaging.send(any()) } throws exception

        val result =
            org.junit.jupiter.api.assertThrows<FirebaseMessagingException> {
                pushNotifier.send(
                    PushMessage(
                        token = "device-token",
                        title = "title",
                        body = "body",
                    ),
                )
            }

        result shouldBe exception
    }
}
