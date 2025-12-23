package de.elegantsoftware.blitzpay.gateways.outbound.truelayer

import de.elegantsoftware.blitzpay.gateways.api.GatewayProvider
import de.elegantsoftware.blitzpay.gateways.api.PaymentResponse
import de.elegantsoftware.blitzpay.gateways.api.PaymentStatus
import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service.TrueLayerPaymentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class TrueLayerProvider(
    private val paymentService: TrueLayerPaymentService
) : GatewayProvider {

    private val logger = LoggerFactory.getLogger(TrueLayerProvider::class.java)

    // Cache to store authorization URLs by payment ID
    private val paymentCache = ConcurrentHashMap<String, PaymentInfo>()

    data class PaymentInfo(
        val paymentId: String,
        val authorizationUrl: String?,
        val createdAt: Long = System.currentTimeMillis()
    )

    override fun getType(): GatewayType = GatewayType.TRUELAYER

    override fun createPayment(
        amount: BigDecimal,
        currency: String,
        merchantId: String,
        description: String
    ): PaymentResponse {
        logger.info("Creating payment via TrueLayer: merchant=$merchantId, amount=$amount")

        val metadata = mapOf(
            "merchant_id" to merchantId,
            "description" to description,
            "reference" to UUID.randomUUID().toString().substring(0, 8)
        )

        return try {
            val response = paymentService.createPayment(
                amount = amount,
                currency = currency,
                merchantId = merchantId,
                description = description,
                metadata = metadata
            )

            // Cache the payment info
            paymentCache[response.id] = PaymentInfo(
                paymentId = response.id,
                authorizationUrl = response.authorizationUrl
            )

            PaymentResponse(
                paymentId = response.id,
                authorizationUrl = response.authorizationUrl,
                qrCodeUrl = response.qrCodeUrl,
                status = mapStatus(response.status)
            )
        } catch (e: Exception) {
            logger.error("Failed to create TrueLayer payment", e)
            throw RuntimeException("Payment creation failed", e)
        }
    }

    override fun getPaymentStatus(paymentId: String): PaymentStatus {
        return try {
            val status = paymentService.getPaymentStatus(paymentId)
            mapStatus(status)
        } catch (e: Exception) {
            logger.error("Failed to get payment status for $paymentId", e)
            PaymentStatus.FAILED
        }
    }

    override fun supportsQrCode(): Boolean = true

    override fun generateQrCode(paymentId: String): ByteArray? {
        val status = getPaymentStatus(paymentId)

        return if (status == PaymentStatus.PENDING) {
            // Try to get authorization URL from cache first
            val cachedInfo = paymentCache[paymentId]
            val url = if (cachedInfo?.authorizationUrl != null) {
                cachedInfo.authorizationUrl
            } else {
                // Fallback to default URL pattern
                "https://payment.truelayer.com/pay/$paymentId"
            }

            generateQRCodeImage(url)
        } else {
            null
        }
    }

    private fun mapStatus(trueLayerStatus: String): PaymentStatus {
        return when (trueLayerStatus.lowercase()) {
            "authorized", "executed", "settled" -> PaymentStatus.SUCCESS
            "failed", "rejected", "declined" -> PaymentStatus.FAILED
            "cancelled" -> PaymentStatus.CANCELLED
            "pending", "authorizing" -> PaymentStatus.PENDING
            else -> PaymentStatus.PENDING
        }
    }

    private fun generateQRCodeImage(text: String): ByteArray {
        // Placeholder for QR code generation
        // In production, use a library like ZXing
        val qrCodeText = "TrueLayer Payment: $text"
        return qrCodeText.toByteArray()
    }

    // Optional: Add cache cleanup method
    fun cleanupCache(olderThanMillis: Long = 24 * 60 * 60 * 1000) { // 24 hours default
        val now = System.currentTimeMillis()
        val keysToRemove = paymentCache.entries
            .filter { now - it.value.createdAt > olderThanMillis }
            .map { it.key }

        keysToRemove.forEach { paymentCache.remove(it) }
        logger.info("Cleaned up ${keysToRemove.size} old cache entries")
    }
}