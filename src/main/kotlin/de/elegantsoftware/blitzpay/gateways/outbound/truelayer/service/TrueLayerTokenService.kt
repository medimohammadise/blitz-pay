package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service

import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client.TrueLayerAuthClient
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config.TrueLayerProperties
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerTokenInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Service
class TrueLayerTokenService(
    private val authClient: TrueLayerAuthClient,
    private val properties: TrueLayerProperties
) {

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    private val logger = LoggerFactory.getLogger(TrueLayerTokenService::class.java)
    private val tokenCache = ConcurrentHashMap<String, TrueLayerTokenInfo>()

    init {
        initializeWithConfiguredToken()
    }

    private fun initializeWithConfiguredToken() {
        if (!properties.isAccessTokenConfigured) {
            logger.info("No valid access token configured, will fetch on demand")
            return
        }

        logger.info("Initializing with configured access token")

        val tokenInfo = TrueLayerTokenInfo(
            accessToken = properties.accessToken,
            tokenType = "Bearer",
            expiresAt = Clock.System.now() + 1.hours,
            refreshToken = null,
            scope = "payments"
        )

        tokenCache[ACCESS_TOKEN_KEY] = tokenInfo
    }

    fun getAccessToken(forceRefresh: Boolean = false): String =
        synchronized(this) {
            val cached = tokenCache[ACCESS_TOKEN_KEY]

            when {
                forceRefresh -> {
                    logger.info("Forcing token refresh")
                    fetchAndCacheToken()
                }

                cached == null -> {
                    logger.info("No cached token found, fetching new token")
                    fetchAndCacheToken()
                }

                isTokenExpired(cached) -> {
                    logger.info("Token expired, refreshing")
                    refreshToken(cached)
                }

                else -> {
                    logger.debug(
                        "Using cached token (expires in {} seconds)",
                        (cached.expiresAt - Clock.System.now()).inWholeSeconds
                    )
                    cached.accessToken
                }
            }
        }

    private fun fetchAndCacheToken(): String =
        try {
            val response = authClient.getAccessToken()

            val tokenInfo = TrueLayerTokenInfo(
                accessToken = response.accessToken,
                tokenType = response.tokenType,
                expiresAt = Clock.System.now() +
                        response.expiresIn.seconds - 60.seconds,
                refreshToken = response.refreshToken,
                scope = response.scope
            )

            tokenCache[ACCESS_TOKEN_KEY] = tokenInfo

            logger.info("Successfully fetched and cached new access token")
            tokenInfo.accessToken
        } catch (e: Exception) {
            logger.error("Failed to fetch access token from TrueLayer", e)

            if (properties.isAccessTokenConfigured) {
                logger.warn("Falling back to configured access token")
                properties.accessToken
            } else {
                throw RuntimeException("Failed to authenticate with TrueLayer", e)
            }
        }

    private fun refreshToken(current: TrueLayerTokenInfo): String {
        val refreshToken = current.refreshToken
            ?: return fetchAndCacheToken()

        return try {
            val response = authClient.refreshAccessToken(refreshToken)

            val newTokenInfo = TrueLayerTokenInfo(
                accessToken = response.accessToken,
                tokenType = response.tokenType,
                expiresAt = Clock.System.now() +
                        response.expiresIn.seconds - 60.seconds,
                refreshToken = response.refreshToken ?: refreshToken,
                scope = response.scope
            )

            tokenCache[ACCESS_TOKEN_KEY] = newTokenInfo

            logger.info("Successfully refreshed access token")
            newTokenInfo.accessToken
        } catch (e: Exception) {
            logger.warn("Failed to refresh token, fetching new one", e)
            fetchAndCacheToken()
        }
    }

    private fun isTokenExpired(token: TrueLayerTokenInfo): Boolean =
        (token.expiresAt - Clock.System.now()) < 30.seconds

    fun getTokenInfo(): TrueLayerTokenInfo? =
        tokenCache[ACCESS_TOKEN_KEY]

    fun getTokenExpiry(): Instant? =
        tokenCache[ACCESS_TOKEN_KEY]?.expiresAt

    fun clearTokens() {
        tokenCache.clear()
        logger.info("Cleared all cached tokens")
    }

    fun getStatus(): Map<String, Any> =
        mapOf<String, Any>(
            "hasCachedToken" to (tokenCache[ACCESS_TOKEN_KEY] != null),
            "isConfiguredToken" to properties.isAccessTokenConfigured,
            "expiresAt" to (getTokenExpiry()?.toString() ?: "unknown"),
            "isExpired" to (tokenCache[ACCESS_TOKEN_KEY]?.let(::isTokenExpired) ?: true)
        )
}
