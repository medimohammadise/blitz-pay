package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service

import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client.TrueLayerApiClient
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config.TrueLayerProperties
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerBeneficiary
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentMethod
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentRequest
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentResponse
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerPaymentStatusResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
@Service
class TrueLayerPaymentService(
    private val properties: TrueLayerProperties,
    private val apiClient: TrueLayerApiClient,
    private val tokenService: TrueLayerTokenService
) {

    private val logger = LoggerFactory.getLogger(TrueLayerPaymentService::class.java)

    fun createPayment(
        amount: BigDecimal,
        currency: String,
        merchantId: String? = null,
        description: String = "",
        metadata: Map<String, String> = emptyMap()
    ): TrueLayerPaymentResponse {
        logger.info("Creating TrueLayer payment: amount=$amount, currency=$currency")

        // Validate that we have a merchant account ID configured
        require(properties.hasValidMerchantAccount) {
            "TrueLayer merchant account ID is not configured"
        }

        val amountInMinor = amount.multiply(BigDecimal(100)).toInt()

        // Build metadata with merchant ID if provided
        val fullMetadata = if (merchantId != null) {
            metadata + mapOf("merchant_id" to merchantId)
        } else {
            metadata
        }

        val paymentRequest = TrueLayerPaymentRequest(
            amountInMinor = amountInMinor,
            currency = currency.uppercase(),
            paymentMethod = TrueLayerPaymentMethod(),
            beneficiary = TrueLayerBeneficiary(
                merchantAccountId = properties.merchantAccountId // Use from properties
            ),
            metadata = fullMetadata
        )

        return apiClient.createPayment(paymentRequest)
    }

    fun getPaymentStatus(paymentId: String): String {
        return try {
            val response = apiClient.getPayment(paymentId)
            response.status
        } catch (e: Exception) {
            logger.error("Failed to get payment status for $paymentId", e)
            "UNKNOWN"
        }
    }

    fun getPaymentDetails(paymentId: String): TrueLayerPaymentStatusResponse {
        return apiClient.getPayment(paymentId)
    }

    fun createMandate(): String {
        // Validate merchant account ID
        require(properties.hasValidMerchantAccount) {
            "TrueLayer merchant account ID is not configured"
        }

        logger.info("Creating mandate for merchant account: ${properties.merchantAccountId}")
        val response = apiClient.createMandate(properties.merchantAccountId)
        return response["id"] as String? ?: throw IllegalStateException("No mandate ID returned")
    }

    // Optional: Add a method to refresh token if needed
    fun refreshToken(): String {
        return tokenService.getAccessToken(forceRefresh = true)
    }
}