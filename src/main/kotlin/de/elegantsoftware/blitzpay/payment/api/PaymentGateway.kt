package de.elegantsoftware.blitzpay.payment.api

import java.math.BigDecimal

/**
 * A generic interface for payment providers. Each implementation (e.g., TrueLayer, Stripe)
 * will handle the specifics of its own API.
 */
interface PaymentGateway {
    /**
     * Returns the unique name of this gateway (e.g., "TRUELAYER", "STRIPE").
     */
    fun getProviderName(): String

    /**
     * Initiates a payment and returns a URL for the user to complete the payment.
     */
    fun createPaymentLink(paymentId: Long, amount: BigDecimal, currency: String): String
}