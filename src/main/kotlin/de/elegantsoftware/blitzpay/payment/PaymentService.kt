package de.elegantsoftware.blitzpay.payment

import de.elegantsoftware.blitzpay.payment.api.PaymentGateway
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PaymentService(
    // Spring automatically injects all beans that implement the PaymentGateway interface
    gateways: List<PaymentGateway>
) {
    // Create a map for easy lookup: "TRUELAYER" -> TrueLayerGateway instance
    private val gatewayMap = gateways.associateBy { it.getProviderName() }

    fun initiatePayment(
        invoiceId: Long,
        amount: BigDecimal,
        currency: String,
        provider: String
    ): String {
        // 1. Create and save a `Payment` entity in a PENDING state.
        val paymentId = 123L // ID from the newly saved Payment entity

        // 2. Find the correct gateway from the map.
        val gateway = gatewayMap[provider]
            ?: throw IllegalArgumentException("Payment provider '$provider' is not supported.")

        // 3. Delegate the payment creation to the specific gateway.
        return gateway.createPaymentLink(paymentId, amount, currency)
    }
}