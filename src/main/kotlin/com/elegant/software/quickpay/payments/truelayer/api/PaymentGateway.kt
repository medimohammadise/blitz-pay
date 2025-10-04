package com.elegant.software.quickpay.payments.truelayer.api
import com.truelayer.java.payments.entities.paymentdetail.Status
import org.springframework.modulith.NamedInterface
import java.net.URI

@NamedInterface("PaymentGateway")

    data class PaymentRequested(
        val orderId: String,
        val amountMinorUnits: Long,
        val currency: String,
        val userDisplayName: String,
        val redirectReturnUri: String
    )
    data class PaymentResult(
        val orderId: String,
        val paymentId: String? = null,
        val redirectURI: URI? = null,
        val status: Status? = null,
        val error: String? = null
        )