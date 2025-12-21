package de.elegantsoftware.blitzpay.gateways.truelayer

import de.elegantsoftware.blitzpay.payment.api.PaymentGateway
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TrueLayerGateway(
    // private val trueLayerApiClient: TrueLayerApiClient // Your client for the TrueLayer API
) : PaymentGateway {

    override fun getProviderName(): String {
        return "TRUELAYER"
    }

    override fun createPaymentLink(paymentId: Long, amount: BigDecimal, currency: String): String {
        println("Connecting to TrueLayer to create a payment link for payment $paymentId...")

        // 1. Call the TrueLayer API to create a payment intent.
        // 2. Get the payment URL from the TrueLayer response.

        // Placeholder URL for demonstration
        return "https://payment.truelayer.com/v1/payment/some-truelayer-payment-id"
    }
}