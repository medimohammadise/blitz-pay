package de.elegantsoftware.blitzpay.merchant.domain

data class BusinessDetailsRequest(
    val phoneNumber: String,
    val businessAddress: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val taxId: String?,
    val businessType: String?
)
