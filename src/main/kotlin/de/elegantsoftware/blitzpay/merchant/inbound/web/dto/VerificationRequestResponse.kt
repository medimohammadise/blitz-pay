package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class VerificationRequestResponse(
    val message: String,
    val merchantEmail: String
)