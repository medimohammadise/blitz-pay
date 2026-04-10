package com.elegant.software.blitzpay.invoice.internal

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class InvoiceRecipient(
    @Column(name = "recipient_name", nullable = false)
    val name: String = "",

    @Column(name = "recipient_email", nullable = false)
    val email: String = ""
)
