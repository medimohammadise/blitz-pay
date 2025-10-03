package com.elegant.software.quickpay.payments.truelayer.domain

object PaymentEvents {
    data class PaymentSettled(val paymentId: String?)
    data class PaymentFailed(val paymentId: String?, val reason: String)
}
