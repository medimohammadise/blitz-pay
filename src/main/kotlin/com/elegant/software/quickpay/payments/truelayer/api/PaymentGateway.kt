package com.elegant.software.quickpay.payments.truelayer.api

import org.springframework.modulith.NamedInterface

@NamedInterface("PaymentGateway")
interface PaymentGateway {
    fun startPayment(cmd: StartPaymentCommand): String

    data class StartPaymentCommand(
        val orderId: String,
        val amountMinorUnits: Long,
        val currency: String,
        val userDisplayName: String,
        val redirectReturnUri: String
    )
}