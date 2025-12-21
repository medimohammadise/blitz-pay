package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class AddressWebRequest(
    val street: String,
    val city: String,
    val state: String? = null,
    val postalCode: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null
)