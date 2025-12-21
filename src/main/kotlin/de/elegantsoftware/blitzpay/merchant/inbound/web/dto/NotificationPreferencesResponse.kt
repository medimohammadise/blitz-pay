package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class NotificationPreferencesResponse(
    val emailNotifications: Boolean,
    val smsNotifications: Boolean
)