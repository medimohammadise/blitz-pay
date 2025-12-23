package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@ConfigurationProperties(prefix = "truelayer")
data class TrueLayerProperties(

    // API Configuration
    val apiBase: String = "https://api.truelayer-sandbox.com",

    val accessToken: String = "dummy-access-token",

    // Authentication (required)
    val clientId: String = "",
    val clientSecret: String = "",

    // Security & Signing
    val keyId: String = "",
    val privateKeyPath: String = "truelayer_priv.pem",

    // Webhook Configuration
    val webhookJku: String = "http://localhost:8080/.well-known/jwks.json",

    // Merchant Configuration
    val merchantAccountId: String = "",

    // Environment & Behavior
    val environment: String = "sandbox",
    val redirectUri: String = "http://localhost:8080/return",
    val httpLogs: Boolean = false,

    // Nested config
    val webhooks: WebhooksConfig = WebhooksConfig(),

    // Timeouts
    val readTimeout: Long = 30_000,
    val connectTimeout: Long = 5_000
) {



    // Computed properties
    val authBaseUrl: String
        get() = when (environment.lowercase()) {
            "production", "live" -> "https://auth.truelayer.com"
            else -> "https://auth.truelayer-sandbox.com"
        }

    val isSandbox: Boolean
        get() = environment.lowercase() == "sandbox"

    val isProduction: Boolean
        get() = !isSandbox

    val hasValidClientCredentials: Boolean
        get() = clientId.isNotBlank() && clientSecret.isNotBlank()

    val hasValidSigningKeys: Boolean
        get() = keyId.isNotBlank() && privateKeyPath.isNotBlank()

    val hasValidMerchantAccount: Boolean
        get() = merchantAccountId.isNotBlank()

    val isAccessTokenConfigured: Boolean
        get() = accessToken.isNotBlank() && accessToken != "dummy-access-token"

    fun toSafeMap(): Map<String, Any> = mapOf(
        "apiBase" to apiBase,
        "environment" to environment,
        "isSandbox" to isSandbox,
        "merchantAccountId" to merchantAccountId,
        "redirectUri" to redirectUri,
        "httpLogs" to httpLogs,
        "clientId" to clientId.mask(4),
        "keyId" to keyId.mask(4),
        "privateKeyPath" to privateKeyPath,
        "webhookJku" to webhookJku,
        "webhooks" to mapOf(
            "environment" to webhooks.environment,
            "maxSkew" to webhooks.maxSkew
        )
    )

    private fun String.mask(visibleChars: Int = 0): String =
        if (length <= visibleChars * 2) "***"
        else "${take(visibleChars)}...${takeLast(visibleChars)}"
}

data class WebhooksConfig(
    val environment: String = "sandbox",
    val maxSkew: Duration = 60.minutes
)