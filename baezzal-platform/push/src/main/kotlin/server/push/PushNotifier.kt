package server.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Component

@Component
class PushNotifier(
    private val firebaseMessaging: FirebaseMessaging,
) {
    fun send(message: PushMessage): String = firebaseMessaging.send(message.toFirebaseMessage())

    private fun PushMessage.toFirebaseMessage(): Message {
        val builder =
            Message
                .builder()
                .setToken(token)
                .setNotification(
                    Notification
                        .builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage(imageUrl)
                        .build(),
                )

        if (data.isNotEmpty()) {
            builder.putAllData(data)
        }

        return builder.build()
    }
}
