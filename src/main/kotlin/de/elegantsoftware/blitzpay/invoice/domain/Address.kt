package de.elegantsoftware.blitzpay.invoice.domain

data class Address(
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null
)