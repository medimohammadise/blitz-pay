package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class AddressWebResponse(
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
    val country: String,
    val phone: String?,
    val email: String?
)