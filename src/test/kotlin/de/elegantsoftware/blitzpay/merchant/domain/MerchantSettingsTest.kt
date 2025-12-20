package de.elegantsoftware.blitzpay.merchant.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MerchantSettingsTest {

    @Test
    fun `default should create settings with default values`() {
        // When
        val settings = MerchantSettings.default()

        // Then
        assertNull(settings.webhookUrl)
        assertTrue(settings.qrCodeEnabled)
        assertEquals("EUR", settings.defaultCurrency)
        assertEquals(SettlementSchedule.DAILY, settings.settlementSchedule)
        assertNull(settings.notificationEmail)
    }

    @Test
    fun `withWebhookUrl should return new instance with updated webhook`() {
        // Given
        val original = MerchantSettings.default()

        // When
        val updated = original.withWebhookUrl("https://hook.com")

        // Then
        assertEquals("https://hook.com", updated.webhookUrl)
        assertEquals(original.qrCodeEnabled, updated.qrCodeEnabled)
        assertEquals(original.defaultCurrency, updated.defaultCurrency)
        assertNotSame(original, updated) // Should be a new instance
    }

    @Test
    fun `withQrCodeEnabled should return new instance with updated qrCodeEnabled`() {
        // Given
        val original = MerchantSettings.default()

        // When
        val updated = original.withQrCodeEnabled(false)

        // Then
        assertEquals(false, updated.qrCodeEnabled)
        assertEquals(original.webhookUrl, updated.webhookUrl)
        assertNotSame(original, updated)
    }

    @Test
    fun `settings should be embeddable and have correct defaults`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )

        // Then
        assertNotNull(merchant.settings)
        assertEquals("EUR", merchant.settings.defaultCurrency)
        assertTrue(merchant.settings.qrCodeEnabled)
    }
}