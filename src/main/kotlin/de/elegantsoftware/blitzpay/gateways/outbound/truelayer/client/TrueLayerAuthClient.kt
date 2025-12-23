package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client

import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config.TrueLayerProperties
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerAuthResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap

@Component
class TrueLayerAuthClient(
    private val properties: TrueLayerProperties,
    @Qualifier("trueLayerRestClient")
    private val authRestClient: RestClient
) {

    private val logger = LoggerFactory.getLogger(TrueLayerAuthClient::class.java)

    fun getAccessToken(): TrueLayerAuthResponse {
        logger.debug("Requesting access token from TrueLayer (environment: ${properties.environment})")

        require(properties.hasValidClientCredentials) {
            "TrueLayer client credentials are not configured"
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
            add("scope", "payments")
        }

        return try {
            val response = authRestClient.post()
                .uri("/connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(formData)
                .retrieve()
                .body(TrueLayerAuthResponse::class.java)

            require(response != null) { "Received null response from TrueLayer auth endpoint" }

            logger.info("Successfully obtained access token, expires in ${response.expiresIn} seconds")
            response
        } catch (e: Exception) {
            logger.error("Failed to obtain access token from TrueLayer", e)
            throw RuntimeException("Authentication failed with TrueLayer", e)
        }
    }

    fun refreshAccessToken(refreshToken: String): TrueLayerAuthResponse {
        logger.debug("Refreshing access token using refresh token")

        require(properties.hasValidClientCredentials) {
            "TrueLayer client credentials are not configured"
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
            add("refresh_token", refreshToken)
        }

        return authRestClient.post()
            .uri("/connect/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(formData)
            .retrieve()
            .body(TrueLayerAuthResponse::class.java)
            ?: throw IllegalStateException("Failed to refresh access token: null response")
    }

    fun validateConfiguration(): Map<String, Any> {
        return mapOf(
            "hasClientCredentials" to properties.hasValidClientCredentials,
            "authBaseUrl" to properties.authBaseUrl,
            "environment" to properties.environment
        )
    }
}