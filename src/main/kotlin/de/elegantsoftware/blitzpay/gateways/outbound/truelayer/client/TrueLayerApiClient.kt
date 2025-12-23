package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client

import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentRequest
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentResponse
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentStatusResponse
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service.TrueLayerSignatureService
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service.TrueLayerTokenService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import kotlin.time.Clock

@Component
class TrueLayerApiClient(
    private val tokenService: TrueLayerTokenService,
    private val signatureService: TrueLayerSignatureService,
    private val restClient: RestClient
) {
    
    private val logger = LoggerFactory.getLogger(TrueLayerApiClient::class.java)
    
    fun createPayment(request: TrueLayerPaymentRequest): TrueLayerPaymentResponse {
        logger.info("Creating TrueLayer payment for amount: ${request.amountInMinor}")
        
        val accessToken = tokenService.getAccessToken()
        
        return restClient.post()
            .uri("/payments")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header("X-Idempotency-Key", generateIdempotencyKey())
            .body(request)
            .retrieve()
            .body(TrueLayerPaymentResponse::class.java) ?: throw IllegalStateException("Failed to create payment")
    }
    
    fun getPayment(paymentId: String): TrueLayerPaymentStatusResponse {
        logger.debug("Fetching payment status for: $paymentId")
        
        val accessToken = tokenService.getAccessToken()
        
        return restClient.get()
            .uri("/payments/$paymentId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .body(TrueLayerPaymentStatusResponse::class.java) ?: throw IllegalStateException("Payment not found")
    }

    fun createMandate(merchantAccountId: String): Map<String, Any> {
        logger.info("Creating mandate for merchant account: $merchantAccountId")

        val accessToken = tokenService.getAccessToken()

        val response = restClient.post()
            .uri("/mandates")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(
                mapOf(
                    "merchant_account_id" to merchantAccountId,
                    "type" to "sweeping"
                )
            )
            .retrieve()
            .body(Map::class.java)
            ?: throw IllegalStateException("Failed to create mandate")

        @Suppress("UNCHECKED_CAST")
        return response as Map<String, Any>
    }
    
    private fun generateIdempotencyKey(): String {
        return "payment_${Clock.System.now()}_${(0..1000).random()}"
    }
}