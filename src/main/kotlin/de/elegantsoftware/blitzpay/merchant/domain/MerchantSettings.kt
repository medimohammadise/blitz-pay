package de.elegantsoftware.blitzpay.merchant.domain

import jakarta.persistence.Embeddable

@Embeddable
data class MerchantSettings(
    var webhookUrl: String? = null,
    var qrCodeEnabled: Boolean = true,
    var defaultCurrency: String = "EUR",
    var settlementSchedule: SettlementSchedule = SettlementSchedule.DAILY,
    var notificationEmail: String? = null
) {
    companion object {
        fun default(): MerchantSettings = MerchantSettings()
    }

    fun withWebhookUrl(url: String?): MerchantSettings = copy(webhookUrl = url)
    fun withQrCodeEnabled(enabled: Boolean): MerchantSettings = copy(qrCodeEnabled = enabled)

}

enum class SettlementSchedule {
    DAILY, WEEKLY, MONTHLY, MANUAL
}