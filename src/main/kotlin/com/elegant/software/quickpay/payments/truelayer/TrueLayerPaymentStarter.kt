package com.elegant.software.quickpay.payments.truelayer

import com.elegant.software.quickpay.payments.truelayer.api.PaymentGateway
import com.elegant.software.quickpay.payments.truelayer.api.PaymentGateway.StartPaymentCommand
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TrueLayerPaymentStarter(
    private val gateway: PaymentGateway
) {
    data class PaymentRequested(
        val orderId: String,
        val amountMinorUnits: Long,
        val currency: String,
        val userDisplayName: String,
        val returnUri: String
    )

    @EventListener
    fun on(e: PaymentRequested) {
        gateway.startPayment(
            StartPaymentCommand(e.orderId, e.amountMinorUnits, e.currency, e.userDisplayName, e.returnUri)
        )
    }
}
