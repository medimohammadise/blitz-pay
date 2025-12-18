package de.elegantsoftware.blitzpay.sales.domain.exceptions

class InvoiceNotFoundException(invoiceId: Long) :
    RuntimeException("Invoice with ID $invoiceId not found")