package de.elegantsoftware.blitzpay.merchant.support.service

import de.elegantsoftware.blitzpay.common.api.exceptions.ValidationException
import de.elegantsoftware.blitzpay.merchant.api.MerchantEventPublisher
import de.elegantsoftware.blitzpay.merchant.domain.*
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.*
import de.elegantsoftware.blitzpay.merchant.support.exception.*
import de.elegantsoftware.blitzpay.merchant.support.mapper.MerchantMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.kotlin.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class MerchantServiceTest {

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var merchantMapper: MerchantMapper

    @Mock
    private lateinit var eventPublisher: MerchantEventPublisher

    private lateinit var merchantService: MerchantService

    @BeforeEach
    fun setUp() {
        merchantService = MerchantService(merchantRepository, merchantMapper, eventPublisher)
    }

    @Test
    fun `createMerchant should save and publish event when successful`() {
        // Given
        val request = CreateMerchantRequest(
            email = "test@merchant.com",
            businessName = "Test Business",
            defaultCurrency = "USD"
        )

        // Create merchant that will be saved
        val savedMerchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business",
            settings = MerchantSettings(defaultCurrency = "USD")
        ).apply {
            publicId = UUID.randomUUID()
        }

        val expectedResponse = MerchantResponse(
            id = savedMerchant.id,
            publicId = savedMerchant.publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.PENDING_VERIFICATION.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "USD"
            ),
            verifiedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantRepository.existsByEmail("test@merchant.com")).thenReturn(false)
        whenever(merchantRepository.save(any())).thenReturn(savedMerchant)
        whenever(merchantMapper.toResponse(savedMerchant)).thenReturn(expectedResponse)

        // When
        merchantService.createMerchant(request)

        // Then
        val captor = argumentCaptor<Merchant>()
        verify(merchantRepository).save(captor.capture())
        verify(eventPublisher).publishMerchantCreated(savedMerchant)

        val capturedMerchant = captor.firstValue // Use .firstValue for readability
        assertEquals("test@merchant.com", capturedMerchant.email)
    }

    @Test
    fun `createMerchant should throw MerchantAlreadyExistsException when email exists`() {
        // Given
        val request = CreateMerchantRequest(
            email = "existing@merchant.com",
            businessName = "Test Business"
        )

        whenever(merchantRepository.existsByEmail("existing@merchant.com")).thenReturn(true)

        // When & Then
        val exception = assertThrows<MerchantAlreadyExistsException> {
            merchantService.createMerchant(request)
        }
        assertEquals("Merchant with email existing@merchant.com already exists", exception.message)
    }

    @Test
    fun `getMerchant should return merchant when found by publicId`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
        }

        val expectedResponse = MerchantResponse(
            id = merchant.id,
            publicId = publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.PENDING_VERIFICATION.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "EUR"
            ),
            verifiedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))
        whenever(merchantMapper.toResponse(merchant)).thenReturn(expectedResponse)

        // When
        val result = merchantService.getMerchant(publicId)

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(publicId, result.publicId)
        verify(merchantRepository).findByPublicId(publicId)
    }

    @Test
    fun `getMerchant should throw MerchantNotFoundException when not found`() {
        // Given
        val publicId = UUID.randomUUID()
        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows<MerchantNotFoundException> {
            merchantService.getMerchant(publicId)
        }
        assertEquals("Merchant with id $publicId not found", exception.message)
    }

    @Test
    fun `verifyMerchant should verify and publish event`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
        }

        val verifiedMerchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
            verify()
        }

        val expectedResponse = MerchantResponse(
            id = verifiedMerchant.id,
            publicId = publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.ACTIVE.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "EUR"
            ),
            verifiedAt = verifiedMerchant.verifiedAt,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))
        whenever(merchantRepository.save(any())).thenReturn(verifiedMerchant)
        whenever(merchantMapper.toResponse(verifiedMerchant)).thenReturn(expectedResponse)

        // When
        val result = merchantService.verifyMerchant(publicId)

        // Then
        assertEquals(expectedResponse, result)
        assertEquals(MerchantStatus.ACTIVE.name, result.status)
        verify(eventPublisher).publishMerchantVerified(verifiedMerchant)
    }

    @Test
    fun `verifyMerchant should throw IllegalArgumentException when already active`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
            verify() // Already verified
        }

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            merchantService.verifyMerchant(publicId)
        }
        assertEquals("Merchant is already active", exception.message)
    }

    @Test
    fun `updateMerchantSettings should update settings when valid`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create("test@merchant.com", "Test Business").apply { this.publicId = publicId }
        val request = UpdateSettingsRequest(defaultCurrency = "USD", webhookUrl = "https://webhook.example.com")

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))
        whenever(merchantRepository.save(any())).thenReturn(merchant)

        // When
        merchantService.updateMerchantSettings(publicId, request)

        // Then
        val captor = argumentCaptor<Merchant>()
        verify(merchantRepository).save(captor.capture())

        assertEquals("USD", captor.firstValue.settings.defaultCurrency)
        assertEquals("https://webhook.example.com", captor.firstValue.settings.webhookUrl)
    }

    @Test
    fun `updateMerchantSettings should throw ValidationException for invalid currency`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
        }

        val request = UpdateSettingsRequest(defaultCurrency = "INVALID")

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))

        // When & Then
        val exception = assertThrows<ValidationException> {
            merchantService.updateMerchantSettings(publicId, request)
        }
        assertEquals("Invalid currency code: INVALID", exception.message)
    }

    @Test
    fun `updateMerchantSettings should throw ValidationException for invalid fee percentage`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
        }

        val request = UpdateSettingsRequest(transactionFeePercentage = 150.0)

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))

        // When & Then
        val exception = assertThrows<ValidationException> {
            merchantService.updateMerchantSettings(publicId, request)
        }
        assertEquals("Transaction fee percentage must be between 0 and 100", exception.message)
    }

    @Test
    fun `deactivateMerchant should deactivate and publish event`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create("test@merchant.com", "Test").apply { this.publicId = publicId; verify() }

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))
        whenever(merchantRepository.save(any())).thenReturn(merchant)

        // When
        merchantService.deactivateMerchant(publicId)

        // Then
        val captor = argumentCaptor<Merchant>()
        verify(merchantRepository).save(captor.capture())
        verify(eventPublisher).publishMerchantDeactivated(captor.firstValue)

        assertEquals(MerchantStatus.INACTIVE, captor.firstValue.status)
    }

    @Test
    fun `deactivateMerchant should throw MerchantInvalidStatusException when already inactive`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
            deactivate() // Already inactive
        }

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))

        // When & Then
        val exception = assertThrows<MerchantInvalidStatusException> {
            merchantService.deactivateMerchant(publicId)
        }
        assertEquals("Merchant is already inactive", exception.message)
    }

    @Test
    fun `deactivateMerchant should NOT throw when merchant is suspended`() {
        // Given
        val publicId = UUID.randomUUID()
        val merchant = Merchant.create(
            email = "test@merchant.com",
            businessName = "Test Business"
        ).apply {
            this.publicId = publicId
            status = MerchantStatus.SUSPENDED
        }

        whenever(merchantRepository.findByPublicId(publicId)).thenReturn(Optional.of(merchant))
        // Use the Kotlin-wrapped any()
        whenever(merchantRepository.save(any())).thenReturn(merchant)

        // When
        merchantService.deactivateMerchant(publicId)

        // Then
        // 1. Create the captor locally
        val captor = argumentCaptor<Merchant>()

        // 2. Capture the value (this helper handles Kotlin's null safety)
        verify(merchantRepository).save(captor.capture())

        // 3. Use .firstValue for the assertion
        assertEquals(MerchantStatus.INACTIVE, captor.firstValue.status)
    }
}