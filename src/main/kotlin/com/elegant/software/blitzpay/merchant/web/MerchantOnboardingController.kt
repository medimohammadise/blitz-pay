package com.elegant.software.blitzpay.merchant.web

import com.elegant.software.blitzpay.merchant.api.MerchantBusinessProfileRequest
import com.elegant.software.blitzpay.merchant.api.MerchantGateway
import com.elegant.software.blitzpay.merchant.api.MerchantPrimaryContactRequest
import com.elegant.software.blitzpay.merchant.api.MerchantSummary
import com.elegant.software.blitzpay.merchant.api.RegisterMerchantRequest
import com.elegant.software.blitzpay.merchant.application.MerchantRegistrationService
import com.elegant.software.blitzpay.merchant.repository.MerchantApplicationRepository
import com.elegant.software.blitzpay.storage.PresignedUpload
import com.elegant.software.blitzpay.storage.StorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateMerchantRequest(
    val legalBusinessName: String,
    val businessType: String,
    val registrationNumber: String,
    val operatingCountry: String,
    val primaryBusinessAddress: String,
    val contactFullName: String,
    val contactEmail: String,
    val contactPhoneNumber: String
)

@Tag(name = "Merchant Onboarding", description = "Endpoints for merchant onboarding and lifecycle")
@RestController
@RequestMapping("/{version:v\\d+(?:\\.\\d+)*}/merchants", version = "1")
class MerchantOnboardingController(
    private val repository: MerchantApplicationRepository,
    private val gateway: MerchantGateway,
    private val merchantRegistrationService: MerchantRegistrationService,
    private val storageService: StorageService
) {

    @Operation(summary = "Register a new merchant (directly ACTIVE, duplicate registration number rejected with 409)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateMerchantRequest): MerchantSummary {
        val application = merchantRegistrationService.register(
            RegisterMerchantRequest(
                businessProfile = MerchantBusinessProfileRequest(
                    legalBusinessName = request.legalBusinessName,
                    businessType = request.businessType,
                    registrationNumber = request.registrationNumber,
                    operatingCountry = request.operatingCountry,
                    primaryBusinessAddress = request.primaryBusinessAddress
                ),
                primaryContact = MerchantPrimaryContactRequest(
                    fullName = request.contactFullName,
                    email = request.contactEmail,
                    phoneNumber = request.contactPhoneNumber
                )
            )
        )
        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt
        )
    }

    @Operation(summary = "Directly activate a merchant application (skips onboarding flow)")
    @PostMapping("/{id}/activate")
    fun activate(@PathVariable id: UUID): MerchantSummary {
        val application = repository.findById(id)
            .orElseThrow { NoSuchElementException("Merchant application not found: $id") }
        
        application.registerDirect()
        repository.save(application)
        
        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt
        )
    }

    @Operation(summary = "Get merchant application details")
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): MerchantSummary {
        val application = repository.findById(id)
            .orElseThrow { NoSuchElementException("Merchant application not found: $id") }

        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt
        )
    }

    @Operation(
        summary = "Get a pre-signed upload URL for the merchant logo",
        description = "Returns a short-lived S3/MinIO PUT URL. " +
            "Flow: call this → upload binary to uploadUrl → call PUT /{id}/logo with the returned storageKey. " +
            "URL expires in 15 minutes."
    )
    @PostMapping("/{id}/logo/upload-url")
    fun logoUploadUrl(@PathVariable id: UUID): PresignedUpload {
        val storageKey = "merchant/$id/logo/${UUID.randomUUID()}"
        return storageService.presignUpload(storageKey, "image/jpeg")
    }

    @Operation(
        summary = "Set merchant logo",
        description = "Records the S3/MinIO storage key of a logo already uploaded via the upload-url endpoint."
    )
    @PutMapping("/{id}/logo")
    fun setLogo(@PathVariable id: UUID, @RequestBody request: SetLogoRequest): MerchantSummary {
        val application = repository.findById(id)
            .orElseThrow { NoSuchElementException("Merchant application not found: $id") }

        application.updateLogo(request.storageKey)
        repository.save(application)

        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt,
            logoStorageKey = application.businessProfile.logoStorageKey
        )
    }
}

data class SetLogoRequest(val storageKey: String)
