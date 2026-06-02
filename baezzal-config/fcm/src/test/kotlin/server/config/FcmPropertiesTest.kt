package server.config

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FcmPropertiesTest {
    @Test
    fun `서비스 계정 JSON 스키마로 직렬화한다`() {
        val properties =
            FcmProperties(
                type = "service_account",
                projectId = "baezzal-9c723",
                privateKeyId = "key-id",
                privateKey = "line-1\\nline-2",
                clientEmail = "firebase-adminsdk@example.com",
                clientId = "client-id",
                authUri = "https://accounts.google.com/o/oauth2/auth",
                tokenUri = "https://oauth2.googleapis.com/token",
                authProviderX509CertUrl = "https://www.googleapis.com/oauth2/v1/certs",
                clientX509CertUrl =
                    "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk%40example.com",
                universeDomain = "googleapis.com",
            )

        properties.toServiceAccount() shouldBe
            mapOf(
                "type" to "service_account",
                "project_id" to "baezzal-9c723",
                "private_key_id" to "key-id",
                "private_key" to "line-1\nline-2",
                "client_email" to "firebase-adminsdk@example.com",
                "client_id" to "client-id",
                "auth_uri" to "https://accounts.google.com/o/oauth2/auth",
                "token_uri" to "https://oauth2.googleapis.com/token",
                "auth_provider_x509_cert_url" to "https://www.googleapis.com/oauth2/v1/certs",
                "client_x509_cert_url" to
                    "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk%40example.com",
                "universe_domain" to "googleapis.com",
            )
    }
}
