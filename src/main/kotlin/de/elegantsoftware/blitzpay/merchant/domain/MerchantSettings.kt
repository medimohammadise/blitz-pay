package de.elegantsoftware.blitzpay.merchant.domain

data class MerchantSettings(
    val defaultCurrency: String = "EUR",
    val language: String = "en",
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val paymentPreferences: PaymentPreferences = PaymentPreferences()
) {
    data class NotificationPreferences(
        val emailNotifications: Boolean = true,
        val smsNotifications: Boolean = false,
        val marketingEmails: Boolean = false
    )

    data class PaymentPreferences(
        val defaultPaymentMethod: String = "card",
        val allowedCurrencies: Set<String> = setOf("EUR", "USD", "GBP"),
        val autoSettle: Boolean = true
    )
}