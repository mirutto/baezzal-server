package server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fcm")
data class FcmProperties(
    val type: String,
    val projectId: String,
    val privateKeyId: String,
    val privateKey: String,
    val clientEmail: String,
    val clientId: String,
    val authUri: String,
    val tokenUri: String,
    val authProviderX509CertUrl: String,
    val clientX509CertUrl: String,
    val universeDomain: String,
) {
    fun toServiceAccount(): Map<String, String> =
        mapOf(
            "type" to type,
            "project_id" to projectId,
            "private_key_id" to privateKeyId,
            "private_key" to privateKey.replace("\\n", "\n"),
            "client_email" to clientEmail,
            "client_id" to clientId,
            "auth_uri" to authUri,
            "token_uri" to tokenUri,
            "auth_provider_x509_cert_url" to authProviderX509CertUrl,
            "client_x509_cert_url" to clientX509CertUrl,
            "universe_domain" to universeDomain,
        )
}
