package de.elegantsoftware.blitzpay.invoice.domain

enum class InvoiceStatus {
    DRAFT,
    ISSUED,
    SENT,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED,
    WRITTEN_OFF
}