package de.elegantsoftware.blitzpay.gateways.api

import java.math.BigDecimal

interface GatewayProvider {
    fun getType(): GatewayType
    fun createPayment(
        amount: BigDecimal,
        currency: String,
        merchantId: String,
        description: String
    ): PaymentResponse
    
    fun getPaymentStatus(paymentId: String): PaymentStatus
    fun supportsQrCode(): Boolean
    fun generateQrCode(paymentId: String): ByteArray?
}



