package server.auth.infrastructure

data class Oauth2AuthorizationRequestCacheData(
    val authorizationUri: String,
    val clientId: String,
    val redirectUri: String,
    val scopes: Set<String>,
    val state: String,
    val additionalParameters: Map<String, String>,
    val registrationId: String,
)
