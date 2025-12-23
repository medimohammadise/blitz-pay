package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client

import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config.TrueLayerProperties
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service.TrueLayerSignatureService
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service.TrueLayerTokenService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID

@Component
class TrueLayerWebhookClient(
    private val properties: TrueLayerProperties,
    private val tokenService: TrueLayerTokenService,
    private val signatureService: TrueLayerSignatureService,
    private val restClient: RestClient
) {

    private val logger = LoggerFactory.getLogger(TrueLayerWebhookClient::class.java)

    fun registerWebhook(
        url: String,
        events: List<String> = listOf("payment_status_changed")
    ): Map<String, Any> {

        logger.info("Registering webhook for URL: $url")

        return restClient.post()
            .uri("/webhooks")
            .headers { headers ->
                headers.addAll(buildHeaders())
                headers.contentType = MediaType.APPLICATION_JSON
            }
            .body(
                mapOf(
                    "url" to url,
                    "events" to events
                )
            )
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
            ?: throw IllegalStateException("Failed to register webhook")
    }

    fun listWebhooks(): List<Map<String, Any>> {
        return restClient.get()
            .uri("/webhooks")
            .headers { headers ->
                headers.addAll(buildHeaders())
            }
            .retrieve()
            .body(
                object : ParameterizedTypeReference<List<Map<String, Any>>>() {}
            )
            ?: emptyList()
    }

    fun deleteWebhook(webhookId: String) {
        restClient.delete()
            .uri("/webhooks/$webhookId")
            .headers { headers ->
                headers.addAll(buildHeaders())
            }
            .retrieve()
            .toBodilessEntity()
    }

    private fun buildHeaders(): HttpHeaders {
        val headers = HttpHeaders()

        // Try to get access token
        try {
            val accessToken = tokenService.getAccessToken()
            headers.setBearerAuth(accessToken)
        } catch (e: Exception) {
            logger.warn("Failed to get access token from token service", e)

            // Fallback to configured token if available
            if (properties.isAccessTokenConfigured) {
                headers.setBearerAuth(properties.accessToken)
            } else {
                throw IllegalStateException("No access token available for TrueLayer API")
            }
        }

        // Add TrueLayer specific headers
        headers["TL-Version"] = "2021-11-22"
        headers["TL-Correlation-Id"] = UUID.randomUUID().toString()

        return headers
    }
}