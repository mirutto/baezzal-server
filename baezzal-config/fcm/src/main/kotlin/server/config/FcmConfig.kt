package server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
@EnableConfigurationProperties(FcmProperties::class)
class FcmConfig {
    @Bean
    fun firebaseApp(
        properties: FcmProperties,
        objectMapper: ObjectMapper,
    ): FirebaseApp {
        val credentials =
            GoogleCredentials.fromStream(
                ByteArrayInputStream(
                    objectMapper.writeValueAsBytes(properties.toServiceAccount()),
                ),
            )
        val options =
            FirebaseOptions
                .builder()
                .setCredentials(credentials)
                .setProjectId(properties.projectId)
                .build()

        return FirebaseApp
            .getApps()
            .firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging =
        FirebaseMessaging.getInstance(firebaseApp)
}
