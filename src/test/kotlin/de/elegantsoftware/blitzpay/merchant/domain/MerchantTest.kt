package de.elegantsoftware.blitzpay.merchant.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class MerchantTest {

    @Test
    fun `create should return merchant with valid input`() {
        // When
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )

        // Then
        assertEquals("test@merchant.com", merchant.email)
        assertEquals("Test Business", merchant.businessName)
        assertEquals(MerchantStatus.PENDING_VERIFICATION, merchant.status)
        assertNotNull(merchant.settings)
        assertEquals("EUR", merchant.settings.defaultCurrency)
        assertNull(merchant.verifiedAt)
    }

    @Test
    fun `create should trim email and business name`() {
        // When
        val merchant = Merchant.create(
            email = "  test@merchant.com  ",
            businessName = "  Test Business  "
        )

        // Then
        assertEquals("test@merchant.com", merchant.email)
        assertEquals("Test Business", merchant.businessName)
    }

    @Test
    fun `create should throw exception when email is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Merchant.create(email = "", businessName = "Test Business")
        }
        assertEquals("Email must not be blank", exception.message)
    }

    @Test
    fun `create should throw exception when business name is blank`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Merchant.create(email = "test@merchant.com", businessName = "   ")
        }
        assertEquals("Business name must not be blank", exception.message)
    }

    @Test
    fun `create should throw exception when email format is invalid`() {
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Merchant.create(email = "invalid-email", businessName = "Test Business")
        }
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `verify should change status to ACTIVE and set verifiedAt`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )

        // When
        merchant.verify()

        // Then
        assertEquals(MerchantStatus.ACTIVE, merchant.status)
        assertNotNull(merchant.verifiedAt)
    }

    @Test
    fun `verify should throw exception when already verified`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )
        merchant.verify()

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            merchant.verify()
        }
        assertEquals("Merchant is already active", exception.message)
    }

    @Test
    fun `verify should throw exception when already inactive`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )
        merchant.deactivate()

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            merchant.verify()
        }
        assertEquals("Merchant is already inactive", exception.message)
    }

    @Test
    fun `deactivate should change status to INACTIVE`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )
        merchant.verify()

        // When
        merchant.deactivate()

        // Then
        assertEquals(MerchantStatus.INACTIVE, merchant.status)
    }

    @Test
    fun `updateSettings should replace existing settings`() {
        // Given
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        )
        val newSettings = MerchantSettings(
            webhookUrl = "https://webhook.example.com",
            qrCodeEnabled = false,
            defaultCurrency = "USD",
            settlementSchedule = SettlementSchedule.WEEKLY,
            notificationEmail = "admin@example.com"
        )

        // When
        merchant.updateSettings(newSettings)

        // Then
        assertEquals("https://webhook.example.com", merchant.settings.webhookUrl)
        assertEquals(false, merchant.settings.qrCodeEnabled)
        assertEquals("USD", merchant.settings.defaultCurrency)
        assertEquals(SettlementSchedule.WEEKLY, merchant.settings.settlementSchedule)
        assertEquals("admin@example.com", merchant.settings.notificationEmail)
    }

    @Test
    fun `MerchantSettings copy methods should work correctly`() {
        // Given
        val settings = MerchantSettings.default()

        // When
        val withWebhook = settings.withWebhookUrl("https://test.com")
        val withQrCode = settings.withQrCodeEnabled(false)

        // Then
        assertEquals("https://test.com", withWebhook.webhookUrl)
        assertEquals(false, withQrCode.qrCodeEnabled)
        assertEquals("EUR", withQrCode.defaultCurrency) // Original unchanged
    }
}