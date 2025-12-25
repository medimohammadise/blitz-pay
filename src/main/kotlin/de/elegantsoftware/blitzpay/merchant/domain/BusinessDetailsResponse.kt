package de.elegantsoftware.blitzpay.merchant.domain

data class BusinessDetailsResponse(
    val success: Boolean,
    val message: String,
    val profileComplete: Boolean
)