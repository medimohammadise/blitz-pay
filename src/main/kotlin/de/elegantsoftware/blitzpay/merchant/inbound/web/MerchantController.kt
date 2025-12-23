package de.elegantsoftware.blitzpay.merchant.inbound.web

import de.elegantsoftware.blitzpay.merchant.api.MerchantService
import de.elegantsoftware.blitzpay.merchant.domain.MerchantId
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.CreateMerchantRequest
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.ErrorResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantStatusResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.UpdateBusinessNameRequest
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.UpdateMerchantSettingsRequest
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.VerificationRequestResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.VerificationResponse
import de.elegantsoftware.blitzpay.merchant.support.exception.MerchantException
import de.elegantsoftware.blitzpay.merchant.support.mapper.MerchantMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchant Management", description = "APIs for managing merchants, including registration, verification, and profile management")
class MerchantController(
    private val merchantService: MerchantService,
    private val merchantMapper: MerchantMapper
) {

    private val logger = LoggerFactory.getLogger(MerchantController::class.java)

    // POST /api/merchants - Create a new merchant
    @PostMapping
    @Operation(summary = "Register a new merchant", description = "Creates a new merchant account with business information and notification preferences")
    fun create(@RequestBody request: CreateMerchantRequest): ResponseEntity<MerchantResponse> {
        logger.info("Creating merchant: ${request.email}")

        val (email, businessName, settings) = merchantMapper.toDomain(request)
        val merchant = merchantService.registerMerchant(email, businessName, settings)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(merchantMapper.toResponse(merchant))
    }

    // GET /api/merchants/verification?token={token} - Verify email
    @GetMapping("/verification")    @Operation(summary = "Verify merchant email", description = "Verifies a merchant's email address using the verification token sent during registration")    fun verifyEmail(@RequestParam token: UUID): ResponseEntity<VerificationResponse> {
        logger.info("Verifying email with token: $token")

        val merchant = merchantService.verifyMerchantEmail(token)

        return ResponseEntity.ok(
            VerificationResponse(
                success = true,
                message = "Email verified successfully",
                merchant = merchantMapper.toResponse(merchant)
            )
        )
    }

    // POST /api/merchants/{publicId}/verification-requests - Resend verification email
    @PostMapping("/{publicId}/verification-requests")
    fun createVerificationRequest(@PathVariable publicId: UUID): ResponseEntity<VerificationRequestResponse> {
        logger.info("Creating verification request for merchant: $publicId")

        val merchant = merchantService.getMerchantByPublicId(publicId)
        merchantService.resendVerificationEmail(merchant.id)

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(
                VerificationRequestResponse(
                    message = "Verification email resent successfully",
                    merchantEmail = merchant.email
                )
            )
    }

    // GET /api/merchants/{publicId} - Get merchant by public ID
    @GetMapping("/{publicId}")
    @Operation(summary = "Get merchant details", description = "Retrieves merchant information using their public identifier")
    fun getByPublicId(@PathVariable publicId: UUID): ResponseEntity<MerchantResponse> {
        val merchant = merchantService.getMerchantByPublicId(publicId)
        return ResponseEntity.ok(merchantMapper.toResponse(merchant))
    }

    // GET /api/merchants/{merchantId}/profile - Get merchant profile (using internal ID)
    @GetMapping("/{merchantId}/profile")
    fun getProfile(@PathVariable merchantId: Long): ResponseEntity<MerchantResponse> {
        val merchant = merchantService.getMerchant(MerchantId(merchantId))
        return ResponseEntity.ok(merchantMapper.toResponse(merchant))
    }

    // GET /api/merchants?status=ACTIVE - List merchants with optional filtering
    @GetMapping
    fun list(
        @RequestParam(required = false) status: de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus?
    ): ResponseEntity<List<MerchantResponse>> {
        val merchants = if (status == de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus.ACTIVE) {
            merchantService.findActiveMerchants()
        } else {
            // In real implementation, you'd have a paginated/filtered search
            emptyList()
        }

        return ResponseEntity.ok(merchants.map { merchantMapper.toResponse(it) })
    }

    // PATCH /api/merchants/{merchantId}/settings - Update merchant settings
    @PatchMapping("/{merchantId}/settings")
    fun updateSettings(
        @PathVariable merchantId: Long,
        @RequestBody request: UpdateMerchantSettingsRequest
    ): ResponseEntity<MerchantResponse> {
        val settings = de.elegantsoftware.blitzpay.merchant.domain.MerchantSettings(
            defaultCurrency = request.defaultCurrency,
            language = request.language,
            notificationPreferences = de.elegantsoftware.blitzpay.merchant.domain.MerchantSettings.NotificationPreferences(
                emailNotifications = request.emailNotifications,
                smsNotifications = request.smsNotifications
            )
        )

        val merchant = merchantService.updateMerchantSettings(MerchantId(merchantId), settings)
        return ResponseEntity.ok(merchantMapper.toResponse(merchant))
    }

    // PUT /api/merchants/{merchantId}/business-name - Update business name
    @PutMapping("/{merchantId}/business-name")
    fun updateBusinessName(
        @PathVariable merchantId: Long,
        @RequestBody request: UpdateBusinessNameRequest
    ): ResponseEntity<MerchantResponse> {
        val merchant = merchantService.updateMerchantBusinessName(
            MerchantId(merchantId),
            request.businessName
        )
        return ResponseEntity.ok(merchantMapper.toResponse(merchant))
    }

    // POST /api/merchants/{merchantId}/deactivation - Deactivate merchant
    @PostMapping("/{merchantId}/deactivation")
    fun deactivate(@PathVariable merchantId: Long): ResponseEntity<MerchantResponse> {
        val merchant = merchantService.deactivateMerchant(MerchantId(merchantId))
        return ResponseEntity.ok(merchantMapper.toResponse(merchant))
    }

    // GET /api/merchants/{publicId}/status - Get merchant status
    @GetMapping("/{publicId}/status")
    fun getStatus(@PathVariable publicId: UUID): ResponseEntity<MerchantStatusResponse> {
        val merchant = merchantService.getMerchantByPublicId(publicId)
        return ResponseEntity.ok(
            MerchantStatusResponse(
                status = merchant.status,
                isActive = merchant.isActive(),
                isEmailVerified = merchant.isEmailVerified()
            )
        )
    }

    @ExceptionHandler(MerchantException::class)
    fun handleMerchantException(ex: MerchantException): ResponseEntity<ErrorResponse> {
        logger.error("Merchant exception occurred: ${ex.errorCode}", ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                code = ex.errorCode.name,
                message = ex.message ?: "An error occurred"
            ))
    }
}
